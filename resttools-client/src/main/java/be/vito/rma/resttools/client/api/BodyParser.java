package be.vito.rma.resttools.client.api;

import java.io.File;

import be.vito.rma.resttools.client.dtos.Response;


/**
 * @author (c) 2017 Stijn.VanLooy@vito.be
 *
 * internal interface
 *
 */
public interface BodyParser<O> {

	/**
	 * Parses body into an O instance in response
	 */
	public void parseBody (String body, Response<O> response);

	/**
	 * Get the target file for a file download
	 * @return null if the request was no file download request
	 */
	public File getTargetFile ();

}
