package be.vito.rma.resttools.client.tools;

import java.util.Base64;
import be.vito.rma.resttools.api.consts.HttpHeaders;
import be.vito.rma.resttools.client.dtos.BasicAuthCredentials;

/**
 * @author (c) 2016-2022 Stijn.VanLooy@vito.be
 *
 */
public class HttpHeadersTools {

	public static void setBasicAuth (final org.springframework.http.HttpHeaders headers, final BasicAuthCredentials credentials) {
		final byte [] bytes = new String (credentials.getUsername()
				+ ":" + credentials.getPassword()).getBytes();
		final String value = "Basic " + new String(Base64.getEncoder().encodeToString(bytes));
		headers.add(HttpHeaders.Authorization, value);
	}

}
