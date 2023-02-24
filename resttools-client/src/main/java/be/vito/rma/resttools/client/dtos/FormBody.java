package be.vito.rma.resttools.client.dtos;

import java.util.HashMap;

/**
 *  Helper class for requests that include parameters in the body.
 *
 *  For example:
 *
 *	  <pre>{@code
 *    grant_type=client_credentials&scope=none
 *    }</pre>
 *
 */
public class FormBody extends HashMap<String, String> {

	private static final long serialVersionUID = 6977708115238552184L;

	public String createBody() {
		final StringBuffer sb = new StringBuffer();
		final String separator = "&";
		for (final String key : keySet()) {
			sb.append(key);
			sb.append("=");
			sb.append(get(key));
			sb.append(separator);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - separator.length(), sb.length());
		}
		return sb.toString();
	}

}
