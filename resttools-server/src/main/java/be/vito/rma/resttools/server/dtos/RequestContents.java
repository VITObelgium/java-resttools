package be.vito.rma.resttools.server.dtos;

import java.io.InputStream;

import org.springframework.http.MediaType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author (c) 2017 Stijn.VanLooy@vito.be
 *
 *  internal DTO
 *
 */
public class RequestContents {

	@Getter @Setter private InputStream inputStream;
	@Getter private String contentType;
	@Getter private MediaType mediaType;

	public void setContentType (final String contentType) {
		this.contentType = contentType;
		if (contentType != null) {
			String [] tokens = contentType.split(";");
			mediaType = MediaType.parseMediaType(tokens[0]);
		} else
			mediaType = null;
	}

}
