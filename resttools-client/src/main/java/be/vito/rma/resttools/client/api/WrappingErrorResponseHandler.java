package be.vito.rma.resttools.client.api;

import static be.vito.rma.resttools.json.JsonTools.fromJson;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.type.TypeReference;

import be.vito.rma.resttools.errors.dtos.ErrorMessage;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * An ErrorResponseHandler that wraps all error responses in a HttpErrorMessagesException
 *
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
@Slf4j
public class WrappingErrorResponseHandler implements ErrorResponseHandler {

	private final HttpErrorMessagesExceptionWrapper wrapper;

	public WrappingErrorResponseHandler(final HttpErrorMessagesExceptionWrapper wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public void handleErrorResponse(final HttpStatus statusCode, final String responseBody, final String requestUrl, final String requestBody) {
		wrapper.setException(parseHttpErrorMessages(statusCode, responseBody, requestUrl, requestBody));
	}

	@Override
	public void handleErrorResponse(final int statusCode, final String responseBody, final String requestUrl, final String requestBody) {
		wrapper.setException(parseHttpErrorMessages(null, responseBody, requestUrl, requestBody));
	}

	@Override
	public void handleErrorResponse(final Exception e, final String requestUrl, final String requestBody) {
		log.error("Request to " + requestUrl + " failed, request body: " + requestBody);
		wrapper.setException(new HttpErrorMessagesException(new ErrorMessage("error.message", e.getMessage())));
	}

	private HttpErrorMessagesException parseHttpErrorMessages (final HttpStatus statusCode, final String responseBody,
			final String requestUrl, final String requestBody) {
		log.error("Request to " + requestUrl + " failed, request body: " + requestBody);
		List<ErrorMessage> messages;
		try {
			messages = fromJson(responseBody,
					new TypeReference<List<ErrorMessage>>() {});
		} catch (final Exception e) {
			log.warn("Failed to parse error response body: " + responseBody);
			if (statusCode != null)
				return new HttpErrorMessagesException(statusCode, new ErrorMessage("http.status", statusCode.getReasonPhrase()), new ErrorMessage("error.message", e.getMessage()));
			else
				return new HttpErrorMessagesException(new ErrorMessage("error.message", e.getMessage()));
		}
		if (statusCode != null)
			return new HttpErrorMessagesException(statusCode, messages);
		else
			return new HttpErrorMessagesException(messages);
	}

}
