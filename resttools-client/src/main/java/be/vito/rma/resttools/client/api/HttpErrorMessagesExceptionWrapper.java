package be.vito.rma.resttools.client.api;

import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import lombok.Getter;
import lombok.Setter;

/**
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
public class HttpErrorMessagesExceptionWrapper {

	@Getter @Setter private HttpErrorMessagesException exception;

}
