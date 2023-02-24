package be.vito.rma.resttools.server.services;

import static be.vito.rma.resttools.json.JsonTools.fromJson;
import static be.vito.rma.resttools.json.JsonTools.toPrettyJson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import com.fasterxml.jackson.core.type.TypeReference;

import be.vito.rma.resttools.api.dtos.DeploymentUrl;
import be.vito.rma.resttools.api.dtos.VoidType;
import be.vito.rma.resttools.errors.enums.ResttoolsErrors;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import be.vito.rma.resttools.json.JsonTools;
import be.vito.rma.resttools.server.api.Factory;
import be.vito.rma.resttools.server.api.FileRequestHandler;
import be.vito.rma.resttools.server.api.RequestHandler;
import be.vito.rma.resttools.server.dtos.FileWrapper;
import be.vito.rma.resttools.server.dtos.RequestContents;
import be.vito.rma.resttools.tools.StreamTools;
import be.vito.rma.resttools.tools.StringTools;
import lombok.extern.slf4j.Slf4j;

/**
 * @author (c) 2020-2022 Stijn.VanLooy@vito.be
 *
 */
@Component
@Slf4j
public class ControllerTools {

	/**
	 * Use this one for simple input types, for example String
	 * @param <I> input type
	 * @param <O> output type
	 * @param request the request to handle
	 * @param response the response that will be sent
	 * @param successResponseHttpStatus the HTTP status to use if successful
	 * @param inputType convert data in request into this input type
	 * @param handler use this RequestHandler to process the request
	 */
	public final <I, O> void handleRequest (final HttpServletRequest request,
			final HttpServletResponse response, final HttpStatus successResponseHttpStatus,
			final Class<I> inputType, final RequestHandler<I, O> handler) {
		setNoCacheHeaders(response);
		try {
			final I input = inputType.equals(VoidType.class) ? null : parseBody(request, inputType);
			final O output = handler.handleRequest(input);
			if (output != null && !output.getClass().equals(VoidType.class))
				setResponse(response, output, successResponseHttpStatus);
			else
				setVoidResponse(response, successResponseHttpStatus);
		} catch (final HttpErrorMessagesException e) {
			setResponse(response, e);
		} catch (final Exception e) {
			log.error("unexpected error while handling " + request.getMethod() + " request " + request.getRequestURL(), e);
			setResponse(response, ResttoolsErrors.UNEXPECTED_ERROR.getException());
		}
	}

	/**
	 * uses default OK HttpStatus when successful
	 * @param <I> input type
	 * @param <O> output type
	 * @param request the request to handle
	 * @param response the response that will be sent
	 * @param inputType convert data in request into this input type
	 * @param handler use this RequestHandler to process the request
	 */
	public final <I, O> void handleRequest (final HttpServletRequest request,
			final HttpServletResponse response,
			final Class<I> inputType, final RequestHandler<I, O> handler) {
		handleRequest(request, response, HttpStatus.OK, inputType, handler);
	}

	/**
	 * Use this one for composed input types, for example {@code List<String>}
	 * @param <I> input type
	 * @param <O> output type
	 * @param request the request to handle
	 * @param response the response that will be sent
	 * @param successResponseHttpStatus the HTTP status to use if successful
	 * @param inputType convert data in request into this input type
	 * @param handler use this RequestHandler to process the request
	 */
	public final <I, O> void handleRequest (final HttpServletRequest request,
			final HttpServletResponse response, final HttpStatus successResponseHttpStatus,
			final TypeReference<I> inputType, final RequestHandler<I, O> handler) {
		setNoCacheHeaders(response);
		try {
			final I input = inputType.equals(VoidType.class) ? null : parseBody(request, inputType);
			final O output = handler.handleRequest(input);
			if (!output.getClass().equals(VoidType.class))
				setResponse(response, output, successResponseHttpStatus);
			else
				setVoidResponse(response, successResponseHttpStatus);
		} catch (final HttpErrorMessagesException e) {
			setResponse(response, e);
		} catch (final Exception e) {
			log.error("unexpected error while handling " + request.getMethod() + " request " + request.getRequestURL(), e);
			setResponse(response, ResttoolsErrors.UNEXPECTED_ERROR.getException());
		}
	}

	/**
	 * uses default OK HttpStatus when successful
	 * @param <I> input type
	 * @param <O> output type
	 * @param request the request to handle
	 * @param response the response that will be sent
	 * @param inputType convert data in request into this input type
	 * @param handler use this RequestHandler to process the request
	 */
	public final <I, O> void handleRequest (final HttpServletRequest request,
			final HttpServletResponse response,
			final TypeReference<I> inputType, final RequestHandler<I, O> handler) {
		handleRequest(request, response, HttpStatus.OK, inputType, handler);
	}

	/**
	 *
	 * @param request the request to handle
	 * @param requiredApiKey if null or empty, all requests will result in invalid_api_key
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public void handleApiKey (final HttpServletRequest request, final String requiredApiKey) throws HttpErrorMessagesException {
		final String actualApiKey = request.getHeader("X-Api-Key");
		if (actualApiKey == null) {
			throw ResttoolsErrors.API_KEY_REQUIRED.getException();
		}
		if (requiredApiKey == null || requiredApiKey.strip().length() == 0 ||
				!actualApiKey.equals(requiredApiKey)) {
			throw ResttoolsErrors.INVALID_API_KEY.getException();
		}
	}

	public DeploymentUrl getDeploymentUrl (final HttpServletRequest request, final String endpointPrefix) {
		final String hostAndPort = getDeploymentHostAndPort(request);
		String host = hostAndPort;
		String port = null;
		if (hostAndPort.contains(":")) {
			final String [] tokens = hostAndPort.split(":");
			host = tokens[0];
			port = tokens[1];
		}
		return new DeploymentUrl(getDeploymentSchema(request), host, port, getDeploymentPrefix(request, endpointPrefix));
	}

	private String getDeploymentSchema (final HttpServletRequest request) {
		if ((request.getHeader("Front-End-Https") != null && request.getHeader("Front-End-Https").equalsIgnoreCase("on"))
				|| (request.getHeader("X-Forwarded-Ssl") != null && request.getHeader("X-Forwarded-Ssl").equalsIgnoreCase("on"))
				|| (request.getHeader("X-Forwarded-Proto") != null && request.getHeader("X-Forwarded-Proto").equalsIgnoreCase("https"))
				|| (request.getHeader("X-Forwarded-Protocol") != null && request.getHeader("X-Forwarded-Protocol").equalsIgnoreCase("https"))
				|| (request.getHeader("X-Url-Scheme") != null && request.getHeader("X-Url-Scheme").equalsIgnoreCase("https"))) {
			/*
			 * if the original request was forwarded, and during the forwarding process, the SSL layer was stripped off
			 * getRequestURL() returns an http URL while the original URL was an https one
			 * we detect this situation here and restore the https protocol part if that is required
			 * see also: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto
			 */
			return "https";
		} else {
			final String url = request.getRequestURL().toString();
			return url.substring(0, url.indexOf("://"));
		}
	}

	private String getDeploymentHostAndPort (final HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		url = url.substring(url.indexOf("://") + 3);
		url = url.substring(0, url.indexOf("/"));
		return url;
	}

	private String getDeploymentPrefix (final HttpServletRequest request, final String endpointPrefix) {
		// strip off endpointPrefix at the end
		final int end = request.getRequestURL().lastIndexOf(endpointPrefix);
		String url = request.getRequestURL().substring(0, end);
		// strip off schema at the start
		url = url.substring(url.indexOf("://") + 3);
		// strip off host at the start
		if (url.contains("/"))
			// deployment prefix is present
			url = url.substring(url.indexOf("/") + 1);
		else
			// empty deployment prefix
			url = "";
		// strip off trailing /
		while (url.endsWith("/"))
			url = url.substring(0, url.length() - 1);
		return url;
	}

	/**
	 * Parse String valued path variables or request parameters
	 * @param data string to parse
	 * @return the parsed string
	 */
	public final String parseString (final String data) {
		if (data == null || data.length() == 0) return null;
		return data;
	}

	/**
	 * Parses a list from a number of candidate lists
	 * The first non-null list is parsed. Other non-null candidates after that first one, are ignored
	 * example use: when supporting serveral HTTP query syntaxes
	 * {@code Spring web already supports param=value1&param=value2 and param=value1,value2
	 * If you want to support param[]=value1&param[]=value2 as well}, you need to explicitly accept
	 * _both_ the "param" and "param[]" query parameters. Then you can use this utility method to
	 * extract the one that was used. The order of the arguments in this utility method determines
	 * the preferred syntax
	 * @param dataCandidate1 the first accepted query parameter name
	 * @param dataCandidate2 the second/alternative accepted query parameter name
	 * @return the parsed list of query parameter values
	 */
	public final List<String> parseStringList (final List<String> dataCandidate1, final List<String> dataCandidate2) {
		return parseStringList(dataCandidate1 != null ? dataCandidate1 : dataCandidate2);
	}


	public final List<String> parseStringList (final List<String> data) {
		if (data == null) return null;
		final List<String> out = new ArrayList<>();
		for (final String str : data) {
			out.add(parseString(str));
		}
		return out;
	}

	/**
	 * Parse integer valued path variables or request parameters
	 * @param data string to parse
	 * @return the parsed integer
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public final Integer parseInt (final String data) throws HttpErrorMessagesException {
		if (data == null || data.length() == 0) return null;
		try {
			return Integer.parseInt(data);
		} catch (final NumberFormatException e) {
			final HttpErrorMessagesException ex = ResttoolsErrors.FAILED_PARSING_LONG.getException(data);
			log.error(ex.getMessage(), e);
			throw ex;
		}
	}

	/**
	 * Parse long valued path variables or request parameters
	 * @param data string to parse
	 * @return the parsed long
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public final Long parseLong (final String data) throws HttpErrorMessagesException {
		if (data == null || data.length() == 0) return null;
		try {
			return Long.parseLong(data);
		} catch (final NumberFormatException e) {
			final HttpErrorMessagesException ex = ResttoolsErrors.FAILED_PARSING_LONG.getException(data);
			log.error(ex.getMessage(), e);
			throw ex;
		}
	}

	/**
	 * Parse boolean valued path variables or request parameters
	 * @param data string to parse
	 * @return the parsed boolean
	 */
	public final Boolean parseBoolean (final String data) {
		if (data == null || data.length() == 0) return null;
		return data.equalsIgnoreCase("true");
	}

	/**
	 * Parse ISO8601 datetime valued path variables or request parameters
	 * @param data string to parse
	 * @return the parsed date
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public final Date parseIso8601TimeStamp (final String data) throws HttpErrorMessagesException {
		if (data == null || data.length() == 0) return null;
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		try {
			return format.parse(data);
		} catch (final ParseException e) {
			final HttpErrorMessagesException ex = ResttoolsErrors.FAILED_PARSING_ISO8601.getException(data);
			log.error(ex.getMessage(), e);
			throw ex;
		}
	}

	public final void sendRawHtml (final HttpServletRequest request, final HttpServletResponse response,
			final Factory<String> rawHtmlFactory) {
		setNoCacheHeaders(response);
		try {
			final String html = rawHtmlFactory.create();
			response.setStatus(HttpStatus.OK.value());
			response.setContentType(MediaType.TEXT_HTML_VALUE);
			StringTools.toStream(html, response.getOutputStream());
		} catch (final HttpErrorMessagesException e) {
			setResponse(response, e);
		} catch (final Exception e) {
			log.error("unexpected error while handling " + request.getMethod() + " request " + request.getRequestURL(), e);
			setResponse(response, ResttoolsErrors.UNEXPECTED_ERROR.getException());
		}
	}

	// -- file upload/download handling -- //

	/**
	 * @param <I> input type
	 * @param request the request to handle
	 * @param response the response that will be sent
	 * @param inputType convert data in request into this input type
	 * @param handler use this RequestHandler to process the request
	 */
	public final <I> void handleFileDownloadRequest (final HttpServletRequest request,
			final HttpServletResponse response, final Class<I> inputType, final RequestHandler<I, FileWrapper> handler) {
		setNoCacheHeaders(response);
		try {
			final I input = inputType.equals(VoidType.class) ? null : parseBody(request, inputType);
			final FileWrapper file = handler.handleRequest(input);
			streamDownloadedFile(file, response);
		} catch (final HttpErrorMessagesException e) {
			setResponse(response, e);
		} catch (final Exception e) {
			log.error("unexpected error while handling " + request.getMethod() + " request " + request.getRequestURL(), e);
			setResponse(response, ResttoolsErrors.UNEXPECTED_ERROR.getException());
		}

	}

	public final <I> void handleFileDownloadRequest (final HttpServletRequest request,
			final HttpServletResponse response, final TypeReference<I> inputType, final RequestHandler<I, FileWrapper> handler) {
		setNoCacheHeaders(response);
		try {
			final I input = inputType.equals(VoidType.class) ? null : parseBody(request, inputType);
			final FileWrapper file = handler.handleRequest(input);
			streamDownloadedFile(file, response);
		} catch (final HttpErrorMessagesException e) {
			setResponse(response, e);
		} catch (final Exception e) {
			log.error("unexpected error while handling " + request.getMethod() + " request " + request.getRequestURL(), e);
			setResponse(response, ResttoolsErrors.UNEXPECTED_ERROR.getException());
		}
	}

	public final <O> void handleFileUploadRequest (final HttpServletRequest request,
			final HttpServletResponse response, final HttpStatus successResponseHttpStatus,
			final FileRequestHandler<O> handler, final long maxSize) {
		setNoCacheHeaders(response);
		try {
			streamUploadedFile(request, handler, maxSize);
			// fetch output
			final O output = handler.handleRequest(new VoidType());
			if (!output.getClass().equals(VoidType.class))
				setResponse(response, output, successResponseHttpStatus);
			else
				setVoidResponse(response, successResponseHttpStatus);
		} catch (final HttpErrorMessagesException e) {
			setResponse(response, e);
		} catch (final Exception e) {
			log.error("unexpected error while handling " + request.getMethod() + " request " + request.getRequestURL(), e);
			setResponse(response, ResttoolsErrors.UNEXPECTED_ERROR.getException());
		}
	}

	/**
	 * uses default OK HttpStatus when successful
	 * @param <O> output type
	 * @param request the request to handle
	 * @param response the response that will be sent
	 * @param handler use this RequestHandler to process the request
	 * @param maxSize maximum file size that is accepted (in bytes)
	 */
	public final <O> void handleFileUploadRequest (final HttpServletRequest request,
			final HttpServletResponse response,
			final FileRequestHandler<O> handler, final long maxSize) {
		handleFileUploadRequest(request, response, HttpStatus.OK, handler, maxSize);
	}

	/**
	 *
	 * @param request the request to handle
	 * @param response the response that will be sent
	 * @param handler use this RequestHandler to process the request
	 * @param maxUploadSize maximum file size that is accepted (in bytes)
	 */
	public final void handleFileExchangeRequest (final HttpServletRequest request,
			final HttpServletResponse response, final FileRequestHandler<FileWrapper> handler, final long maxUploadSize) {
		setNoCacheHeaders(response);
		try {
			streamUploadedFile(request, handler, maxUploadSize);
			final FileWrapper file = handler.handleRequest(new VoidType());
			streamDownloadedFile(file, response);
		} catch (final HttpErrorMessagesException e) {
			setResponse(response, e);
		} catch (final Exception e) {
			log.error("unexpected error while handling " + request.getMethod() + " request " + request.getRequestURL(), e);
			setResponse(response, ResttoolsErrors.UNEXPECTED_ERROR.getException());
		}
	}

	/**
	 * Get the names of the file parts in a multipart request
	 * @param request the request containing the multipart body
	 * @return an Iterator instance yielding the names of the file parts in the multipart body
	 */
	public final Iterator<String> getFilePartNames (final HttpServletRequest request) {
		final MultipartHttpServletRequest mhsr = new StandardMultipartHttpServletRequest(request);
		return mhsr.getFileNames();
	}

	/**
	 * Extract the file metadata of the file part with given name from a multipart request
	 * @param request the request containing the multipart body
	 * @param filePartName the name of the file part
	 * @return the metadata of the given file part
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public final MultipartFile getFilePartMetaData (final HttpServletRequest request, final String filePartName) throws HttpErrorMessagesException {
		final MultipartHttpServletRequest mhsr = new StandardMultipartHttpServletRequest(request);
		final MultipartFile out = mhsr.getFile(filePartName);
		if (out == null) throw ResttoolsErrors.MULTIPART_MISSING_PART.getException(filePartName);
		return out;
	}

	/**
	 * Stream a file part from a multipart request into a given target file
	 * Will not overwrite existing files. Parent directory must exist.
	 * @param request the request containing the multipart body
	 * @param filePartName the name of the file part
	 * @param maxSize maximum accepted file size in bytes
	 * @param targetFile stream contents to this file
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public final void parseFilePart (final HttpServletRequest request, final String filePartName, final long maxSize, final File targetFile) throws HttpErrorMessagesException {
		final MultipartFile mpf = getFilePartMetaData(request, filePartName);
		if (mpf.getSize() > maxSize) {
			throw ResttoolsErrors.FILE_TOO_LARGE_NAMED.getException(filePartName, maxSize);
		}
		try {
			Files.copy(mpf.getInputStream(), targetFile.toPath());
		} catch (final IOException e) {
			log.error("Failed streaming file " + filePartName +
					" from multipart request body into " + targetFile.getAbsolutePath(), e);
			throw ResttoolsErrors.UNEXPECTED_ERROR.getException();
		}
	}

	/**
	 * Get the names of the non-file parts in a multipart request
	 * @param request the request containing the multipart body
	 * @return an Iterator instance yielding the names of the non-file parts in the multipart body
	 */
	public final Iterator<String> getPartNames (final HttpServletRequest request) {
		return new Iterator<>() {

			private final Enumeration<String> names = request.getParameterNames();

			@Override
			public boolean hasNext() {
				return names.hasMoreElements();
			}

			@Override
			public String next() {
				return names.nextElement();
			}
		};
	}

	/**
	 * Extract the value of the JSON part with given name from a multipart request
	 * Use this one for simple input types, for example String
	 * @param <T> output type
	 * @param request the request containing the multipart body
	 * @param partName the name of the part
	 * @param outputType the expected output type
	 * @return the parsed instance of the given output type
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public final <T> T parseJsonPart (final HttpServletRequest request, final String partName, final Class<T> outputType) throws HttpErrorMessagesException {
		final String json = getRawPart(request, partName);
		return JsonTools.fromJson(json, outputType);
	}

	/**
	 * Extract the value of the JSON part with given name from a multipart request
	 * Use this one for composed input types, for example {@code List<String>}
	 * @param <T> output type
	 * @param request the request containing the multipart body
	 * @param partName the name of the part
	 * @param outputType the expected output type
	 * @return the parsed instance of the given output type
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public final <T> T parseJsonPart (final HttpServletRequest request, final String partName, final TypeReference<T> outputType) throws HttpErrorMessagesException {
		final String json = getRawPart(request, partName);
		return JsonTools.fromJson(json, outputType);
	}

	/**
	 * Extract the value of the String part with given name from a multipart request
	 * @param request the request containing the multipart body
	 * @param partName the name of the part
	 * @return the String value of the given part
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public final String parseStringPart (final HttpServletRequest request, final String partName) throws HttpErrorMessagesException {
		return getRawPart(request, partName);
	}

	private final String getRawPart (final HttpServletRequest request, final String partName) throws HttpErrorMessagesException {
		final String data = request.getParameter(partName);
		if (data == null) throw ResttoolsErrors.MULTIPART_MISSING_PART.getException(partName);
		return data;
	}




	// TODO: how to avoid code duplication here?

	private final <T> T parseBody (final HttpServletRequest request, final Class<T> type) throws HttpErrorMessagesException {
		final RequestContents rc = getRequestContents(request);
		if (rc.getContentType() != null) {
			// check content type
			if (rc.getMediaType().equals(MediaType.APPLICATION_JSON) ||
					rc.getMediaType().equals(MediaType.APPLICATION_JSON_UTF8))
				return parseJsonBody(rc.getInputStream(), type);
			// FIXME: limit content-length in all requests, not only file uploads
			// TODO: return String if content type is text
			if (rc.getMediaType().equals(MediaType.APPLICATION_FORM_URLENCODED)) {
				log.warn("Got media type " + rc.getContentType() + ": returning null body and expecting form data in the URL query parameters, not in the body.");
				return null;
			}
		}
		log.warn("Unsupported media type: " + rc.getContentType() + ", trying to parse JSON");
		return parseJsonBody(rc.getInputStream(), type);
	}

	private final <T> T parseBody (final HttpServletRequest request, final TypeReference<T> type) throws HttpErrorMessagesException {
		final RequestContents rc = getRequestContents(request);
		if (rc.getContentType() != null) {
			// check content type
			if (rc.getMediaType().equals(MediaType.APPLICATION_JSON) ||
					rc.getMediaType().equals(MediaType.APPLICATION_JSON_UTF8))
				return parseJsonBody(rc.getInputStream(), type);
			// FIXME: limit content-length in all requests, not only file uploads
			// TODO: return String if content type is text
			if (rc.getMediaType().equals(MediaType.APPLICATION_FORM_URLENCODED)) {
				log.warn("Got media type " + rc.getContentType() + ": returning null body and expecting form data in the URL query parameters, not in the body.");
				return null;
			}
		}
		log.warn("Unsupported media type: " + rc.getContentType() + ", trying to parse JSON");
		return parseJsonBody(rc.getInputStream(), type);
	}

	private final <T> T parseJsonBody (final InputStream is, final Class<T> type) throws HttpErrorMessagesException {
		try {
			return fromJson(is, type);
		} catch (final Exception e) {
			log.warn("Failed parsing " + type.getName() + " from request body", e);
			throw ResttoolsErrors.FAILED_PARSING_JSON.getException();
		}
	}

	private final <T> T parseJsonBody (final InputStream is, final TypeReference<T> type) throws HttpErrorMessagesException {
		try {
			return fromJson(is, type);
		} catch (final Exception e) {
			log.warn("Failed parsing " + type.getType().getTypeName() + " from request body", e);
			throw ResttoolsErrors.FAILED_PARSING_JSON.getException();
		}
	}

	private final RequestContents getRequestContents (final HttpServletRequest request) throws HttpErrorMessagesException {
		final RequestContents rc = new RequestContents();
		rc.setContentType(request.getContentType());
		try {
			rc.setInputStream(request.getInputStream());
		} catch (IOException | IllegalStateException e) {
			log.error("Error while extracting request contents", e);
			throw ResttoolsErrors.UNEXPECTED_ERROR.getException();
		}
		return rc;
	}

	private final void setResponse (final HttpServletResponse response, final HttpErrorMessagesException e) {
		response.setStatus(e.getHttpStatus().value());
		writeResponseBody(response, e.getMessages());
	}

	private final void setResponse (final HttpServletResponse response, final Object o, final HttpStatus responseStatus) {
		response.setStatus(responseStatus.value());
		writeResponseBody(response, o);
	}

	private final void setVoidResponse (final HttpServletResponse response, final HttpStatus responseStatus) {
		response.setStatus(responseStatus.value());
	}

	private final void writeResponseBody (final HttpServletResponse response, final Object o) {
		try {
			if (o instanceof String) {
				// if body contains a String, don't use the JSON mapper
				response.setContentType(MediaType.TEXT_PLAIN_VALUE);
				StringTools.toStream((String) o, response.getOutputStream());
			} else {
				response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
					toPrettyJson(response.getOutputStream(), o);
			}
		} catch (final IOException e) {
			log.error("failed writing response body", e);
		}
		// TODO: encode responses based upon Accept header in request
	}


	/*
	 * One should never cache REST API responses
	 */
	private void setNoCacheHeaders (final HttpServletResponse response) {
		// HTTP 1.1
		response.setHeader(org.springframework.http.HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
		// HTTP 1.0
		response.setHeader(org.springframework.http.HttpHeaders.PRAGMA, "no-cache");
		// Proxies
		response.setHeader(org.springframework.http.HttpHeaders.EXPIRES, "0");
	}

	private <O> void streamUploadedFile (final HttpServletRequest request, final FileRequestHandler<O> handler, final long maxSize) throws HttpErrorMessagesException, IOException {
		// check if uploaded file is not too large
		if (request.getContentLengthLong() > maxSize) {
			throw ResttoolsErrors.FILE_TOO_LARGE.getException(maxSize);
		}
		// FIXME: also throw FILES_TOO_LARGE if the file is larger than reported in the content length
		// stream file
		request.getInputStream();
		final File targetFile = handler.getTargetFile();
		final FileOutputStream fos = new FileOutputStream(targetFile);
		StreamTools.copy(request.getInputStream(), fos);
		fos.close();
	}

	private void streamDownloadedFile (final FileWrapper source, final HttpServletResponse response)
			throws IOException, HttpErrorMessagesException {
		if (!source.getFile().isFile())
			throw ResttoolsErrors.FILE_NOT_FOUND.getException();
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM.toString());
		response.setHeader(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
				source.getReportedFilename() + "\"");
		response.setContentLengthLong(source.getFile().length());
		final FileInputStream fis = new FileInputStream(source.getFile());
		StreamTools.copy(fis, response.getOutputStream());
		fis.close();
	}

}
