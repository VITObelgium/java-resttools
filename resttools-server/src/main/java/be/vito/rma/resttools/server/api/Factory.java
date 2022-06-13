package be.vito.rma.resttools.server.api;

import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;

/**
 * @author (c) 2019 Stijn.VanLooy@vito.be
 *
 */
public interface Factory<T> {

	public T create () throws HttpErrorMessagesException;

}
