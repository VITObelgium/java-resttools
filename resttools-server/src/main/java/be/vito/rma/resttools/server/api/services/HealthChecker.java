package be.vito.rma.resttools.server.api.services;

import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public interface HealthChecker {

	/**
	 * @throws HttpErrorMessagesException with code 500 if not ok
	 */
	public void doHealthcheck () throws HttpErrorMessagesException;

}
