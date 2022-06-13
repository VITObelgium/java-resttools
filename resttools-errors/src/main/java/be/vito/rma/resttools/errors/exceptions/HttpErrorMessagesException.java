package be.vito.rma.resttools.errors.exceptions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;

import be.vito.rma.resttools.errors.dtos.ErrorMessage;
import lombok.Getter;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public class HttpErrorMessagesException extends Exception {

	private static final long serialVersionUID = -7168298811332747448L;

	@Getter private List<ErrorMessage> messages;
	private final HttpStatus httpStatus;

	/**
	 * Use default HTTP status (400 bad request)
	 * @param messages
	 */
	public HttpErrorMessagesException (final List<ErrorMessage> messages) {
		super();
		this.httpStatus = HttpStatus.BAD_REQUEST;
		this.messages = messages;
	}

	/**
	 * Use default HTTP status (400 bad request)
	 * @param messages
	 */
	public HttpErrorMessagesException (final ErrorMessage ... messages) {
		super();
		this.httpStatus = HttpStatus.BAD_REQUEST;
		this.messages = Arrays.asList(messages);
	}

	public HttpErrorMessagesException (final HttpStatus httpStatus, final ErrorMessage ... messages) {
		super();
		this.httpStatus = httpStatus;
		this.messages = Arrays.asList(messages);
	}

	public HttpErrorMessagesException (final HttpStatus httpStatus, final List<ErrorMessage> messages) {
		super();
		this.httpStatus = httpStatus;
		this.messages = messages;
	}

	public HttpStatus getHttpStatus () {
		return httpStatus;
	}

	public void addMessage (final ErrorMessage message) {
		final List<ErrorMessage> oldList = messages;
		messages = new ArrayList<>();
		messages.add(message);
		for (final ErrorMessage m: oldList)
			messages.add(m);
	}

	@Override
	public String getMessage () {
		final StringBuilder sb = new StringBuilder();
		sb.append(httpStatus.value() + " " + httpStatus.getReasonPhrase() + ": ");

		try {
			final StringBuilder mb = new StringBuilder();
			boolean first = true;
			for (final ErrorMessage m : messages) {
				if (first)
					first = false;
				else
					mb.append("; ");
				mb.append(MessageFormat.format(m.getMessage(), m.getArguments()));
			}
			sb.append(mb.toString());
		} catch (final IllegalArgumentException e) {
			sb.append("failed to parse error messages from server: ");
			boolean first = true;
			for (final ErrorMessage m : messages) {
				if (first)
					first = false;
				else
					sb.append("; ");
				sb.append("message: ");
				sb.append(m.getMessage());
				sb.append(", arguments: ");
				boolean firstArg = true;
				for (final Object arg : m.getArguments()) {
					if (firstArg)
						firstArg = false;
					else
						sb.append(", ");
					sb.append(arg.toString());
				}
			}
		}
		return sb.toString();
	}

	public boolean containsErrorMessageKey (final String key) {
		for (final ErrorMessage message: messages)
			if (message.getKey().equals(key))
				return true;
		return false;
	}


}
