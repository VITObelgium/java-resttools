package be.vito.rma.resttools.client.dtos;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public class Response<O> {

	@Getter @Setter private String requestUrl;
	@Getter @Setter private HttpStatus httpStatus;
	@Getter @Setter private HttpHeaders headers;
	@Getter @Setter private O body;

}
