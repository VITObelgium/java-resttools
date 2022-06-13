package be.vito.rma.resttools.client.exceptions;

/**
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 * Thrown if an ErrorResponseHandler has been called.
 * The exception provides no further information. The handler should have handled the error properly.
 * The exception is thrown as well to avoid unwanted behavior (null returns, silent/undetected error propagation, ..)
 */
public class ErrorResponseException extends Exception {

	private static final long serialVersionUID = 223379651483816062L;

}
