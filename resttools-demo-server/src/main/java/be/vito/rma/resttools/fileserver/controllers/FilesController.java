package be.vito.rma.resttools.fileserver.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;

import be.vito.rma.configtools.common.api.ConfigurationService;
import be.vito.rma.resttools.api.dtos.DeploymentUrl;
import be.vito.rma.resttools.api.dtos.VoidType;
import be.vito.rma.resttools.errors.enums.ResttoolsErrors;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import be.vito.rma.resttools.fileserver.tools.QueryParameterTools;
import be.vito.rma.resttools.server.api.FileRequestHandler;
import be.vito.rma.resttools.server.dtos.FileWrapper;
import be.vito.rma.resttools.server.services.ControllerTools;
import lombok.extern.slf4j.Slf4j;

/**
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
@RestController
@Slf4j
public class FilesController {

	@Autowired private ControllerTools cTools;
	@Autowired private ConfigurationService config;

	private String filePrefix;
	private long maxSize;
	private long fileIndex = 0;

	@PostConstruct
	public void init () {
		filePrefix = config.getString("files.prefix");
		if (!filePrefix.endsWith(File.separator))
			filePrefix = filePrefix + File.separator;
		final File dir = new File(filePrefix);
		if (!dir.exists())
			dir.mkdirs();
		if (!dir.isDirectory())
			throw new RuntimeException(filePrefix + " already exists and is not a directory");
		maxSize = config.getLong("files.size.limit");
	}

	@RequestMapping(value = "upload", method = RequestMethod.PUT)
	public void upload (final HttpServletRequest request, final HttpServletResponse response) {
		QueryParameterTools.printQueryParameters(request);
		cTools.handleFileUploadRequest(request, response, new FileRequestHandler<String>() {

			File targetFile;

			@Override
			public String handleRequest(final VoidType input) throws HttpErrorMessagesException {
				System.out.println("File upload streamed into " + targetFile.getAbsolutePath());
				return targetFile.getName();
			}

			private void createFile () {
				targetFile = new File(filePrefix + "data_" + fileIndex + ".dat");
				fileIndex++;
			}

			@Override
			public File getTargetFile() {
				createFile();
				while (targetFile.exists())
					createFile();
				return targetFile;
			}
		}, maxSize);
	}

	@RequestMapping(value = "download/{name:.*}", method = RequestMethod.GET)
	public void downloadGet (final HttpServletRequest request, final HttpServletResponse response,
			@PathVariable(value = "name") final String filenameData) {
		QueryParameterTools.printQueryParameters(request);
		cTools.handleFileDownloadRequest(request, response, VoidType.class, v -> {
			final String filename = cTools.parseString(filenameData);
			return new FileWrapper(getFile(filename));
		});
	}

	@RequestMapping(value = "download/{name:.*}", method = RequestMethod.PUT)
	public void downloadPut (final HttpServletRequest request, final HttpServletResponse response,
			@PathVariable(value = "name") final String filenameData) {
		QueryParameterTools.printQueryParameters(request);
		cTools.handleFileDownloadRequest(request, response, DeploymentUrl.class, du -> {
			final String filename = cTools.parseString(filenameData);
			System.out.println("got deployment url " + du);
			return new FileWrapper(getFile(filename));
		});
	}

	@RequestMapping(value = "download/{name:.*}", method = RequestMethod.POST)
	public void downloadPost (final HttpServletRequest request, final HttpServletResponse response,
			@PathVariable(value = "name") final String filenameData) {
		QueryParameterTools.printQueryParameters(request);
		cTools.handleFileDownloadRequest(request, response, new TypeReference<List<String>>() {}, list -> {
			final String filename = cTools.parseString(filenameData);
			System.out.println("got strings:");
			for (final String str : list)
				System.out.println(str);
			return new FileWrapper(getFile(filename));
		});
	}

	@RequestMapping(value = "download/{name:.*}", method = RequestMethod.DELETE)
	public void downloadDelete (final HttpServletRequest request, final HttpServletResponse response,
			@PathVariable(value = "name") final String filenameData) {
		QueryParameterTools.printQueryParameters(request);
		cTools.handleFileDownloadRequest(request, response, new TypeReference<List<String>>() {}, list -> {
			final String filename = cTools.parseString(filenameData);
			System.out.println("got strings:");
			for (final String str : list)
				System.out.println(str);
			return new FileWrapper(getFile(filename));
		});
	}

	@RequestMapping(value = "exchange", method = RequestMethod.PUT)
	public void exchange (final HttpServletRequest request, final HttpServletResponse response) {
		QueryParameterTools.printQueryParameters(request);
		cTools.handleFileExchangeRequest(request, response, new FileRequestHandler<FileWrapper>() {

			private File targetFile;

			private void createFile () {
				targetFile = new File(filePrefix + "exchange_" + fileIndex + ".dat");
				fileIndex++;
			}

			@Override
			public File getTargetFile() {
				// save uploaded file here
				createFile();
				while (targetFile.exists())
					createFile();
				return targetFile;
			}

			@Override
			public FileWrapper handleRequest(final VoidType input) throws HttpErrorMessagesException {
				// return the uploaded file in the response
				return new FileWrapper(targetFile);
			}
		}, maxSize);
	}

	private File getFile (final String filename) throws HttpErrorMessagesException {
		final File file = new File(filePrefix + filename);
		try {
			// file must be located in filePrefix dir and must exist
			if (!file.getCanonicalPath().startsWith(filePrefix) || !file.exists())
				throw ResttoolsErrors.FILE_NOT_FOUND.getException();
		} catch (final IOException e) {
			log.warn("Error while checking file '" + filename + "'", e);
			throw ResttoolsErrors.FILE_NOT_FOUND.getException();
		}
		return file;
	}

}
