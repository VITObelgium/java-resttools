package be.vito.rma.resttools.errors.dtos;

import lombok.Getter;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public class ErrorMessage {

	public ErrorMessage () {
		super();
	}

	public ErrorMessage(final String key, final String message, final Object... arguments) {
		super();
		this.key = key;
		this.message = message;
		this.arguments = arguments;
	}

	@Getter private String key;
	@Getter private String message;
	@Getter private Object [] arguments;

}
