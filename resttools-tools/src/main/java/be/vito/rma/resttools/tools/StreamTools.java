package be.vito.rma.resttools.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import be.vito.rma.resttools.tools.progress.ProgressListener;
import be.vito.rma.resttools.tools.progress.ProgressProcessHandler;

/**
 * @author (c) 2017 Stijn.VanLooy@vito.be
 *
 */
public class StreamTools {

	public static void copy (final InputStream is, final OutputStream os) {
		copy (is, os, -1, null);
	}

	public static void copy (final InputStream is, final OutputStream os,
			final long expectedByteCount, final ProgressListener listener) {
		final ProgressProcessHandler handler = new ProgressProcessHandler();
		handler.run(() -> {

			final byte [] buffer = new byte[16 * 1024];
			int len;
			try {
				while ((len = is.read(buffer)) > 0) {
					os.write(buffer, 0, len);
					os.flush();
					handler.add2actual(len);
				}
			} catch (final IOException e) {
				// handler will deal with the exception
				throw new RuntimeException(e);
			} finally {
				try {
					os.close();
					is.close();
				} catch (final IOException e) {
					// handler will deal with the exception
					throw new RuntimeException(e);
				}
			}

		}, listener, expectedByteCount);
	}


}
