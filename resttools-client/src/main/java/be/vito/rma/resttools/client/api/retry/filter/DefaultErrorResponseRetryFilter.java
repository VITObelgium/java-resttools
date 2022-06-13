package be.vito.rma.resttools.client.api.retry.filter;

import org.springframework.http.HttpStatus;

/**
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
public class DefaultErrorResponseRetryFilter implements ErrorResponseRetryFilter {

	private boolean retry (final int code) {
		if (code == HttpStatus.GONE.value()
				|| code == HttpStatus.UNAUTHORIZED.value()
				|| code == HttpStatus.FORBIDDEN.value()) {
			// resource is no longer available (GONE)
			// or authentication failed (UNAUTHORIZED, FORBIDDEN)
			// => no use retrying
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean retry(final HttpStatus statusCode, final String responseBody, final String requestUrl, final String requestBody) {
		return retry (statusCode.value());
	}

	@Override
	public boolean retry(final int statusCode, final String responseBody, final String requestUrl, final String requestBody) {
		return retry (statusCode);
	}

	@Override
	public boolean retry(final Exception e, final String requestUrl, final String requestBody) {
		// always retry when getting an Exception
		return true;
	}

}
