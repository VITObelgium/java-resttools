package be.vito.rma.resttools.client.api;

import java.io.IOException;

import org.springframework.http.HttpStatus;

import be.vito.rma.resttools.json.JsonTools;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * An ErrorResponseHandler that logs all error responses.
 *
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
@Slf4j
public class LoggingErrorResponseHandler implements ErrorResponseHandler {

	@Override
	public void handleErrorResponse(final HttpStatus statusCode, final String responseBody, final String requestUrl, final String requestBody) {
		final String prettyResponse = prettify(responseBody);
		final StringBuilder sb = new StringBuilder();
		sb.append("Error in response from "); sb.append(requestUrl); sb.append(": ");
		sb.append(statusCode.value()); sb.append(" "); sb.append(statusCode.getReasonPhrase());
		sb.append("\nRequest body:\n");
		sb.append(requestBody);
		sb.append("\nResponse body:\n");
		sb.append(responseBody);
		if (prettyResponse != null) {
			sb.append("\nPrettified response body:\n");
			sb.append(prettyResponse);
		}
		log.error(sb.toString());
	}

	@Override
	public void handleErrorResponse(final int statusCode, final String responseBody, final String requestUrl, final String requestBody) {
		final String prettyResponse = prettify(responseBody);
		final StringBuilder sb = new StringBuilder();
		sb.append("Error in response from "); sb.append(requestUrl); sb.append(": ");
		sb.append(statusCode);
		sb.append("\nRequest body:\n");
		sb.append(requestBody);
		sb.append("\nResponse body:\n");
		sb.append(responseBody);
		if (prettyResponse != null) {
			sb.append("\nPrettified response body:\n");
			sb.append(prettyResponse);
		}
		log.error(sb.toString());
	}

	@Override
	public void handleErrorResponse(final Exception e, final String requestUrl, final String requestBody) {
		log.error("Error consuming REST endpoint at " + requestUrl + " with request body:\n" + requestBody, e);
	}

	private String prettify (final String json) {
		if (json == null) return null;
		try {
			return JsonTools.prettifyJson(json);
		} catch (final IOException e) {
			// failed prettifying: no problem
		}
		return null;
	}

}
