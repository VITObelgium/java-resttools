package be.vito.rma.resttools.spring;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public class SpringContextTools {

	/**
	 * @param resourceName must start with '/'
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static FileSystemXmlApplicationContext loadContext (
			final String resourceName,
			final HashMap<String, String> parameters) throws IOException {
		if (!resourceName.startsWith("/"))
			throw new RuntimeException("resource name must start with /");
		File appCtxFile = File.createTempFile("tmp", ".xml");
		appCtxFile.deleteOnExit();
		InputStream is = SpringContextTools.class.getResourceAsStream(resourceName);
		PrintWriter writer = new PrintWriter(appCtxFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null) {
			for (Entry<String, String> entry : parameters.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				line = line.replace(key, value);
			}
			writer.println(line);
		}
		writer.close();
		return new FileSystemXmlApplicationContext("/" + appCtxFile.getAbsolutePath());
	}
}
