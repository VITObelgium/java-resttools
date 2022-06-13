package be.vito.rma.resttools.server.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import be.vito.rma.resttools.api.dtos.Healthcheck;
import be.vito.rma.resttools.api.dtos.VoidType;
import be.vito.rma.resttools.server.api.services.HealthChecker;
import be.vito.rma.resttools.server.services.ControllerTools;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
@RestController
public class HealthcheckController {

	@Autowired ControllerTools tools;
	@Autowired HealthChecker healthChecker;

	@RequestMapping(value = "/healthcheck", method = RequestMethod.GET)
	public void doHealthcheck (final HttpServletRequest request, final HttpServletResponse response) {
		tools.handleRequest(request, response, VoidType.class, v -> {
			healthChecker.doHealthcheck();
			final Healthcheck h = new Healthcheck();
			h.setStatus("OK");
			return h;
		});
	}

}
