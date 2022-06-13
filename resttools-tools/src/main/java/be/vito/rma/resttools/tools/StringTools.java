package be.vito.rma.resttools.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * @author (c) 2017 Stijn.VanLooy@vito.be
 *
 */
public class StringTools {

	public static String toString (final InputStream is) {
		Scanner streamScanner = new Scanner(is, "utf-8");
		Scanner scanner = streamScanner.useDelimiter("\\Z");
		String out = scanner.hasNext() ? scanner.next() : null;
		streamScanner.close();
		return out;
	}

	public static void toStream (final String source, final OutputStream target) {
		try {
			target.write(source.getBytes("utf-8"));
		} catch (IOException e) {
			throw new RuntimeException ("Failed streaming String to OutputStream", e);
		}
	}

}
