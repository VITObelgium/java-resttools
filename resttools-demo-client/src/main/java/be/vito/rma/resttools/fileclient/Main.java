package be.vito.rma.resttools.fileclient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpMethod;

import be.vito.rma.resttools.api.dtos.DeploymentUrl;
import be.vito.rma.resttools.api.dtos.VoidType;
import be.vito.rma.resttools.client.api.ErrorResponseHandler;
import be.vito.rma.resttools.client.api.LoggingErrorResponseHandler;
import be.vito.rma.resttools.client.api.retry.scheme.NoRetryScheme;
import be.vito.rma.resttools.client.connection.DefaultRestConnection;
import be.vito.rma.resttools.client.connection.RestConnection;
import be.vito.rma.resttools.client.dtos.Endpoint;
import be.vito.rma.resttools.client.dtos.MultiPartBody;
import be.vito.rma.resttools.client.dtos.RequestParameter;
import be.vito.rma.resttools.client.dtos.Response;
import be.vito.rma.resttools.client.exceptions.ErrorResponseException;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;

/**
 * @author (c) 2018-2022 Stijn.VanLooy@vito.be
 *
 */
public class Main {

	public static void main(final String[] args) throws ErrorResponseException, IOException {

		final String serviceUrl = "http://localhost:8080/demo/";
		final Endpoint endpoint = new Endpoint("multipart");

		final ErrorResponseHandler errorResponseHandler = new LoggingErrorResponseHandler();

		// for request parameter (encoding/decoding) testing only, has nothing to do with file transfers
		final RequestParameter [] testRequestParameters = new RequestParameter [] {
				new RequestParameter("funky chars", "= & / : ? +"),
				new RequestParameter("one by one", "/a/"),
				new RequestParameter("one by one", "?b?"),
				new RequestParameter("one by one", ":c:"),
				new RequestParameter("one by one", "%d%"),
				new RequestParameter("one by one", " e "),
				new RequestParameter("one by one", "+f+"),
		};

		RestConnection conn;
		try {
			conn = new DefaultRestConnection(serviceUrl);
		} catch (final HttpErrorMessagesException e) {
			throw new RuntimeException(e);
		}
		// disable retry on error
		conn.setRetryScheme(new NoRetryScheme());

		// multipart/form-data POST file upload

		final MultiPartBody mpd = new MultiPartBody();
		mpd.addStringPart("String", "hello world!");
		final DeploymentUrl du = new DeploymentUrl("http", "somewhere.com", "8080", "my_api");
		mpd.addJsonPart("json", du);
		mpd.addFilePart("file", new File("pom.xml"));

		Response<String> response = conn.consume(endpoint, HttpMethod.POST, mpd, String.class, errorResponseHandler, testRequestParameters);

		System.out.println(response.getHttpStatus().value() + " " + response.getHttpStatus().getReasonPhrase());

		// octet-stream file upload

		response = conn.consume(new Endpoint("upload"), HttpMethod.PUT, new File("pom.xml"), String.class, errorResponseHandler, testRequestParameters);
		System.out.println(response.getHttpStatus().value() + " " + response.getHttpStatus().getReasonPhrase());
		final String serverFilename = response.getBody();
		System.out.println("Saved on server as " + serverFilename);

		// octet-stream file download

		// with GET request (no payload allowed in request body)
		File targetFile = Files.createTempFile("demo client get", ".dat").toFile();
		conn.downloadFile(new Endpoint("download", serverFilename), HttpMethod.GET, new VoidType(), targetFile, errorResponseHandler, testRequestParameters);
		System.out.println("Downloaded file into " + targetFile.getAbsolutePath());

		// with PUT request (DeploymentUrl payload in request body)
		targetFile = Files.createTempFile("demo client put", ".dat").toFile();
		conn.downloadFile(new Endpoint("download", serverFilename), HttpMethod.PUT, du, targetFile, errorResponseHandler, testRequestParameters);
		System.out.println("Downloaded file into " + targetFile.getAbsolutePath());

		// with POST request (with a List payload in request body)
		targetFile = Files.createTempFile("demo client post", ".dat").toFile();
		final List<String> colors = new ArrayList<>();
		colors.add("red"); colors.add("green"); colors.add("blue");
		conn.downloadFile(new Endpoint("download", serverFilename), HttpMethod.POST, colors, targetFile, errorResponseHandler, testRequestParameters);
		System.out.println("Downloaded file into " + targetFile.getAbsolutePath());

		// with DELETE request (with a List payload in request body)
		targetFile = Files.createTempFile("demo client delete", ".dat").toFile();
		conn.downloadFile(new Endpoint("download", serverFilename), HttpMethod.DELETE, colors, targetFile, errorResponseHandler, testRequestParameters);
		System.out.println("Downloaded file into " + targetFile.getAbsolutePath());

		// octet-stream file exchange (upload in request, download through response)
		final File sourceFile = new File("pom.xml");
		targetFile = Files.createTempFile("demo client exchange", ".dat").toFile();
		conn.downloadFile(new Endpoint("exchange"), HttpMethod.PUT, sourceFile, targetFile, errorResponseHandler, testRequestParameters);
		System.out.println("Downloaded file into " + targetFile.getAbsolutePath());

	}

}
