package be.vito.rma.resttools.fileserver.services;

import org.springframework.stereotype.Component;

import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import be.vito.rma.resttools.server.api.services.HealthChecker;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
@Component
public class HealthCheckerImpl implements HealthChecker {

	@Override
	public void doHealthcheck() throws HttpErrorMessagesException {
		// nothing to do here: if we can call this, everything is ok
	}

}
