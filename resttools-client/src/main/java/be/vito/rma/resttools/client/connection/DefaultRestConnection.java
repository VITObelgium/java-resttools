package be.vito.rma.resttools.client.connection;

import org.springframework.http.HttpHeaders;

import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public class DefaultRestConnection extends AbstractRestConnection {

	/**
	 * A REST connection that does NO health nor version checks
	 * @param serviceUrl URL of the REST service to use
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public DefaultRestConnection (final String serviceUrl) throws HttpErrorMessagesException {
		this(serviceUrl, null);
	}

	/**
	 * A REST connection that does a health and version check before connecting
	 * @param serviceUrl URL of the REST service to use
	 * @param requiredVersionPrefix version of the REST service must start with this prefix
	 * @throws HttpErrorMessagesException if something went wrong
	 */
	public DefaultRestConnection (final String serviceUrl, final String requiredVersionPrefix) throws HttpErrorMessagesException {
		super(serviceUrl, requiredVersionPrefix);
	}

	@Override
	protected HttpHeaders createRequestHeaders () {
		// nothing to do here
		return new HttpHeaders();
	}

	@Override
	protected void handleResponseHeaders (final HttpHeaders headers, final String requestUrl) {
		// nothing to do here
	}

}
