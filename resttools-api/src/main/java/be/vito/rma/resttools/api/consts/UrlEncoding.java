package be.vito.rma.resttools.api.consts;

/**
 * @author (c) 2020 Stijn.VanLooy@vito.be
 *
 */
public class UrlEncoding {

	/**
	 * These chars are still problematic when used in URL's
	 * neither Spring nor OkHttp handle those chars nicely on their own
	 * The combination Spring and OkHttp as implemented in UrlTools handles more chars correctly,
	 * but still fails on these chars in some cases.
	 * Also see: be.vito.rma.resttools.client.tools.UrlTools in resttools-client
	 */
	public static final char [] URL_ENCODING_TO_BE_AVOIDED_CHARS = new char [] {';','/', '\\'};

	public static String getUrlEncodingToBeAvoidedCharsAsString () {
		final StringBuilder sb = new StringBuilder();
		for (final char c : URL_ENCODING_TO_BE_AVOIDED_CHARS) {
			sb.append(c);
		}
		return sb.toString();
	}

}
