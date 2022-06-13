package be.vito.rma.resttools.fileserver.controllers;

import java.io.File;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.vito.rma.configtools.common.api.ConfigurationService;
import be.vito.rma.resttools.api.dtos.DeploymentUrl;
import be.vito.rma.resttools.api.dtos.VoidType;
import be.vito.rma.resttools.fileserver.tools.QueryParameterTools;
import be.vito.rma.resttools.server.services.ControllerTools;

/**
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
@RestController
public class MultiPartController {

	@Autowired private ControllerTools cTools;
	@Autowired private ConfigurationService config;

	private String filePrefix;
	private long maxSize;

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

	@RequestMapping(value = "/multipart", method = RequestMethod.POST)
	public void upload (final HttpServletRequest request, final HttpServletResponse response) {
		QueryParameterTools.printQueryParameters(request);
		cTools.handleRequest(request, response, VoidType.class, v -> {
			/*
			 * tomcat caches multipart bodies into /var/cache/tomcat8/Catalina/localhost/fs/
			 * this avoids OutOfMemoryExceptions
			 * Note: in Jetty, this code only works for small files: Jetty loads multipart bodies
			 * in memory before the request handling even starts
			 */
			System.out.println("Data parts:");
			/*
			 * Note: iterating over all available names is not required.
			 * If you know which part names are in the request (most likely through the API definition),
			 * you can parse the (file) parts right away.
			 * The parse methods also check if the part with given name exists.
			 * If not, the parse methods will throw an appropriate HttpErrorMessagesException
			 */
			Iterator<String> names = cTools.getPartNames(request);
			while (names.hasNext()) {
				final String name = names.next();
				System.out.println(name + ": " + cTools.parseStringPart(request, name));
				if (name.equals("json")) {
					final DeploymentUrl deploymentUrl = cTools.parseJsonPart(request, name, DeploymentUrl.class);
					System.out.println(name + " parsed from JSON into DeploymentUrl object: " + deploymentUrl);
				}
			}
			System.out.println();

			System.out.println("File parts:");
			names = cTools.getFilePartNames(request);
			while (names.hasNext()) {
				final String name = names.next();
				final MultipartFile mpf = cTools.getFilePartMetaData(request, name);
				final File targetFile = new File(filePrefix + mpf.getOriginalFilename());
				if (targetFile.exists()) targetFile.delete();
				cTools.parseFilePart(request, name, maxSize, targetFile);
				System.out.println(name + ": saved into " + targetFile);
			}

			return new VoidType();
		});
	}

}
