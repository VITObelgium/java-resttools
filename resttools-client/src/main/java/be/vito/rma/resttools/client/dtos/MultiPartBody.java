package be.vito.rma.resttools.client.dtos;

import static be.vito.rma.resttools.json.JsonTools.toPrettyJson;

import java.io.File;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;

/**
 *
 *  Helper class for doing multipart/form-data post requests
 *  for Example:
 *
 *  <pre>{@code
 *  RestConnection conn = new DefaultRestConnection("https://some.rest.api/");
 *  MultiPartBody multiPartBody = new MultiPartBody();
 *  multiPartBody.addStringPart("part name", "part value");
 *  SomeDto data = new SomeDto();
 *  multiPartBody.addJsonPart("json part name", data);
 *  multiPartBody.addFilePart("file part name", new File("/some/file"));
 *  Response<VoidType> response = conn.consume(new Endpoint("endpoint_name"), HttpMethod.POST,
 *      multiPartBody, VoidType.class, new LoggingErrorResponseHandler());
 *	}</pre>
 *
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
public class MultiPartBody extends LinkedMultiValueMap<String, Object> {

	private static final long serialVersionUID = -1825552245399085520L;

	public <T> void addJsonPart(final String name, final T data) {
		add(name, toPrettyJson(data));
	}

	public void addStringPart(final String name, final String data) {
		add(name, data);
	}

	public void addFilePart (final String name, final File file) {
		add(name, new FileSystemResource(file));
	}

}
