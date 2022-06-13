package be.vito.rma.resttools.client.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * @author (c) 2020 Stijn.VanLooy@vito.be
 *
 */
public class BasicAuthCredentials {

	public BasicAuthCredentials() {
		super();
	}

	public BasicAuthCredentials(final String username, final String password) {
		super();
		this.username = username;
		this.password = password;
	}

	@Getter @Setter private String username;
	@Getter @Setter private String password;

}
