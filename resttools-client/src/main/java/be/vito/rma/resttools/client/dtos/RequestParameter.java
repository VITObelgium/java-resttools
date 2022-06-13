package be.vito.rma.resttools.client.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public class RequestParameter {

	public RequestParameter() {
		super();
	}

	public RequestParameter (final String key, final Object value) {
		super();
		this.key = key;
		this.value = value.toString();
	}

	public RequestParameter(final String key, final int value) {
		this(key, "" + value);
	}

	public RequestParameter(final String key, final long value) {
		this(key, "" + value);
	}

	public RequestParameter(final String key, final boolean value) {
		this(key, "" + value);
	}

	public RequestParameter(final String key, final float value) {
		this(key, "" + value);
	}

	public RequestParameter(final String key, final double value) {
		this(key, "" + value);
	}

	public RequestParameter(final String key, final byte value) {
		this(key, "" + value);
	}

	public RequestParameter(final String key, final short value) {
		this(key, "" + value);
	}

	public RequestParameter(final String key, final char value) {
		this(key, "" + value);
	}

	@Getter @Setter private String key;
	@Getter @Setter private String value;


}
