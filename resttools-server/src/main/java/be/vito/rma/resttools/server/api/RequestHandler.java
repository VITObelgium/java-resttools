package be.vito.rma.resttools.server.api;

import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public interface RequestHandler<I, O> {

	public O handleRequest (I input) throws HttpErrorMessagesException;

}
