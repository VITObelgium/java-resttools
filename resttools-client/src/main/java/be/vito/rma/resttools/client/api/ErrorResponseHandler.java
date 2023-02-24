package be.vito.rma.resttools.client.api;

import org.springframework.http.HttpStatus;

/**
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
public interface ErrorResponseHandler {

	/**
	 * called if an error response (status code != 200 OK) is received.
	 * @param statusCode the status code that was received
	 * @param responseBody the body of the error response
	 * @param requestUrl the request URL that resulted in the error response
	 * @param requestBody the request body that resulted in the error response
	 */
	public void handleErrorResponse(final HttpStatus statusCode, final String responseBody,
			final String requestUrl, final String requestBody);

	/**
	 * called if an error response (status code != 200 OK) is received
	 * and the received status code is unknown to the Spring framework
	 * @param statusCode the status code that was received
	 * @param responseBody the body of the error response
	 * @param requestUrl the request URL that resulted in the error response
	 * @param requestBody the request body that resulted in the error response
	 */
	public void handleErrorResponse(final int statusCode, final String responseBody,
			final String requestUrl, final String requestBody);

	/**
	 * Called if an exception occurred without receiving a response from the server
	 * for example a ResourceAccessException (caused by for example a timeout) or
	 * unexpected (Runtime)Exceptions.
	 * @param e the exception that was thrown
	 * @param requestUrl the request URL that resulted in the error response
	 * @param requestBody the request body that resulted in the error response
	 */
	public void handleErrorResponse(Exception e, final String requestUrl, final String requestBody);

}
