package be.vito.rma.resttools.fileclient;

import java.io.File;
import java.io.IOException;
import org.springframework.http.HttpMethod;

import be.vito.rma.resttools.api.dtos.VoidType;
import be.vito.rma.resttools.client.api.ErrorResponseHandler;
import be.vito.rma.resttools.client.api.LoggingErrorResponseHandler;
import be.vito.rma.resttools.client.api.retry.scheme.NoRetryScheme;
import be.vito.rma.resttools.client.connection.DefaultRestConnection;
import be.vito.rma.resttools.client.connection.RestConnection;
import be.vito.rma.resttools.client.dtos.Endpoint;
import be.vito.rma.resttools.client.dtos.MultiPartBody;
import be.vito.rma.resttools.client.dtos.Response;
import be.vito.rma.resttools.client.exceptions.ErrorResponseException;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;

/**
 * @author (c) 2020-2022 Stijn.VanLooy@vito.be
 *
 */
public class LargeFileTest {

	public static void main(final String[] args) throws HttpErrorMessagesException, ErrorResponseException, IOException {

		final File sourceFile = new File("/home/vlooys/Downloads/iso/debian-live-10.4.0-amd64-gnome.iso");	// 2.6 GB

		final File targetFile = new File("/dev/shm/targetfile_largefile_test.iso");

		final String serviceUrl = "http://localhost:8080/demo/";

		final ErrorResponseHandler errorResponseHandler = new LoggingErrorResponseHandler();

		final RestConnection conn = new DefaultRestConnection(serviceUrl);
		conn.setRetryScheme(new NoRetryScheme());

		// upload
		long time = System.currentTimeMillis();
		Response<String> response = conn.consume(new Endpoint("upload"), HttpMethod.PUT, sourceFile, String.class, errorResponseHandler);
		System.out.println(response.getHttpStatus().value() + " " + response.getHttpStatus().getReasonPhrase());
		String serverFilename = response.getBody();
		System.out.println("Saved on server as " + serverFilename);
		System.out.println("Uploaded in " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");

		// download
		time = System.currentTimeMillis();
		conn.downloadFile(new Endpoint("download", serverFilename), HttpMethod.GET, new VoidType(), targetFile, errorResponseHandler);
		System.out.println("Downloaded file into " + targetFile.getAbsolutePath());
		System.out.println("Downloaded in " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");
		// avoid No space left on device: clear everything
		targetFile.delete();
		System.out.println("Make sure there is enough space in /dev/shm/ now!");

		// exchange
		time = System.currentTimeMillis();
		conn.downloadFile(new Endpoint("exchange"), HttpMethod.PUT, sourceFile, targetFile, errorResponseHandler);
		System.out.println("Downloaded file (exchange) into " + targetFile.getAbsolutePath());
		System.out.println("Exchanged in " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");
		// avoid No space left on device: clear everything
		targetFile.delete();
		System.out.println("Make sure there is enough space in /dev/shm/ now!");

		// multipart upload
		time = System.currentTimeMillis();
		final MultiPartBody mpd = new MultiPartBody();
		mpd.addStringPart("String", "hello world!");
		mpd.addFilePart("file", sourceFile);
		mpd.addStringPart("Another String", "hello again!");
		response = conn.consume(new Endpoint("multipart"), HttpMethod.POST, mpd, String.class, errorResponseHandler);
		System.out.println(response.getHttpStatus().value() + " " + response.getHttpStatus().getReasonPhrase());
		serverFilename = response.getBody();
		System.out.println("Saved on server as " + serverFilename);
		System.out.println("Uploaded (multipart) in " + ((System.currentTimeMillis() - time) / 1000.0) + " seconds");

	}

}
