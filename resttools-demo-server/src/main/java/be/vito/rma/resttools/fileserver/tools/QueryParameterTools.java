package be.vito.rma.resttools.fileserver.tools;

import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

/**
 * @author (c) 2019 Stijn.VanLooy@vito.be
 *
 */
public class QueryParameterTools {

	/*
	 * This does not illustrate file handling in any way.
	 * This was only added to be able to test query parameter encoding/decoding.
	 */
	public static void printQueryParameters (final HttpServletRequest request) {
		System.out.println("got query parameters:");
		for (final Entry<String, String []> entry : request.getParameterMap().entrySet()) {
			System.out.println("  " + entry.getKey() + ":");
			for (final String value : entry.getValue()) {
				System.out.println("    " + value);
			}
		}
	}

}
