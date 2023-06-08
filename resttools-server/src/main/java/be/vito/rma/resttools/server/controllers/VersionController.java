package be.vito.rma.resttools.server.controllers;

import javax.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import be.vito.rma.configtools.common.api.ConfigurationService;
import be.vito.rma.resttools.api.dtos.Version;
import be.vito.rma.resttools.api.dtos.VoidType;
import be.vito.rma.resttools.server.services.ControllerTools;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
@RestController
public class VersionController {

	@Autowired private ControllerTools tools;
	@Autowired private ConfigurationService config;

	private String version;

	@PostConstruct
	public void init () {
		version = config.getVersion();
	}

	@RequestMapping(value = "/version", method = RequestMethod.GET)
	public void getVersion (final HttpServletRequest request, final HttpServletResponse response) {
		tools.handleRequest(request, response, VoidType.class, v -> {
			final Version version = new Version();
			version.setVersion(this.version);
			return version;
		});
	}

	@RequestMapping(value = "/stijn", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public String stijn () {
		return "<html><body><pre>&#32;&#32;&#32;&#1641;&#40;&#94;&#8255;&#94;&#41;&#1782;&#32;&#10;&#477;&#633;&#477;&#613;&#32;&#115;&#592;&#653;&#32;&#117;&#638;&#7433;&#647;&#83;&#10;</pre></body></html>";
	}

}
