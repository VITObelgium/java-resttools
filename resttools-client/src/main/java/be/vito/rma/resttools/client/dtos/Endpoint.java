package be.vito.rma.resttools.client.dtos;

import lombok.Getter;

/**
 * @author (c) 2016-2021 Stijn.VanLooy@vito.be
 *
 */
public class Endpoint {

	@Getter private final Object [] pathElements;

	public Endpoint(final Object ... pathElements) {
		super();
		this.pathElements = pathElements;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final Object pathElement : pathElements) {
			if (first)
				first = false;
			else
				sb.append("/");
			sb.append(pathElement);
		}
		return sb.toString();
	}

}
