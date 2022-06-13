package be.vito.rma.resttools.client.connection;

import org.springframework.http.HttpHeaders;

import be.vito.rma.resttools.client.dtos.BasicAuthCredentials;
import be.vito.rma.resttools.client.tools.HttpHeadersTools;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import lombok.Setter;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public class BasicAuthRestConnection extends AbstractRestConnection {

	/*
	 * setter allows to use the same connection instance to consume
	 * endpoints with different credentials,
	 * thereby avoiding repeated version and healthcheck checks
	 * each time the credentials are changed
	 */
	@Setter private BasicAuthCredentials credentials;

	/**
	 * A Basic Auth REST connection that does NO health nor version checks
	 * @param serviceUrl
	 * @param credentials
	 * @throws HttpErrorMessagesException
	 */
	public BasicAuthRestConnection (final String serviceUrl, final BasicAuthCredentials credentials) throws HttpErrorMessagesException {
		this(serviceUrl, null, credentials);
	}

	/**
	 * A Basic Auth REST connection that does a health and version check before connecting
	 * @param serviceUrl
	 * @param requiredVersionPrefix
	 * @param credentials
	 * @throws HttpErrorMessagesException
	 */
	public BasicAuthRestConnection (final String serviceUrl, final String requiredVersionPrefix,
			final BasicAuthCredentials credentials) throws HttpErrorMessagesException {
		super (serviceUrl, requiredVersionPrefix);
		this.credentials = credentials;
	}

	@Override
	protected HttpHeaders createRequestHeaders () {
		final HttpHeaders headers = new HttpHeaders();
		HttpHeadersTools.setBasicAuth(headers, credentials);
		return headers;
	}

	@Override
	protected void handleResponseHeaders (final HttpHeaders headers, final String requestUrl) {
		// nothing to do here
	}

}
