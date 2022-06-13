package be.vito.rma.resttools.errors.enums;

import org.springframework.http.HttpStatus;

import be.vito.rma.resttools.errors.dtos.ErrorMessage;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;

/**
 * @author (c) 2016-2022 Stijn.VanLooy@vito.be
 *
 */
public enum ResttoolsErrors {

	UNSUPPORTED_VERSION (HttpStatus.BAD_REQUEST, "Unsupported version: got {0}, need {1}"),
	CONNECTION_FAILED (HttpStatus.BAD_REQUEST, "Failed to connect to service at {0}"),

	API_KEY_REQUIRED (HttpStatus.UNAUTHORIZED, "API key required"),
	INVALID_API_KEY (HttpStatus.UNAUTHORIZED, "Invalid API key provided"),

	FILE_NOT_FOUND (HttpStatus.NOT_FOUND, "File not found"),
	FILE_TOO_LARGE_NAMED (HttpStatus.PAYLOAD_TOO_LARGE, "File {0} is too large, maximum size is {1} bytes"),
	FILE_TOO_LARGE (HttpStatus.PAYLOAD_TOO_LARGE, "File is too large, maximum size is {0} bytes"),

	FAILED_PARSING_JSON (HttpStatus.BAD_REQUEST , "Failed parsing JSON from request body"),
	FAILED_PARSING_LONG (HttpStatus.BAD_REQUEST, "Failed parsing long from {0}"),
	FAILED_PARSING_ISO8601 (HttpStatus.BAD_REQUEST, "Failed parsing ISO8601 datetime (YYYY-MM-DDThh:mm:ssZZZ:zz) from {0} (example valid value: 2021-04-16T10:29:21+02:00)"),

	MULTIPART_MISSING_PART (HttpStatus.UNPROCESSABLE_ENTITY, "Required part {0} is missing in the multipart request."),

	INVALID_LIMIT (HttpStatus.BAD_REQUEST, "Invalid limit (page size): {0} limit must be within [1,{1}]"),
	INVALID_OFFSET (HttpStatus.BAD_REQUEST, "Invalid offset (page index): {0} offset must be >= 0"),

	VALUE_REQUIRED (HttpStatus.BAD_REQUEST, "A (non-null) value is required"),
	READ_ONLY_CONFIGURATION_PARAMETER (HttpStatus.BAD_REQUEST, "{0} is a read-only configuration parameter: it can not be changed"),
	UNKNOWN_CONFIGURATION_PARAMETER (HttpStatus.BAD_REQUEST, "Unknown configuration parameter: {0}"),
	INVALID_CONFIGURATION_PARAMETER_TYPE (HttpStatus.BAD_REQUEST, "Invalid type for configuration parameter {0}: got {1}, expected {2}"),

	UNEXPECTED_ERROR (HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");

	private String message;
	private HttpStatus httpStatus;

	private ResttoolsErrors (final HttpStatus httpStatus, final String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}

	public ErrorMessage getMessage (final Object ... arguments) {
		return new ErrorMessage(toString(), message, arguments);
	}

	public HttpErrorMessagesException getException (final Object ... arguments) {
		return new HttpErrorMessagesException(httpStatus, getMessage(arguments));
	}

	@Override
	public String toString () {
		return super.toString().toLowerCase();
	}

}
