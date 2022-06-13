package be.vito.rma.resttools.fileserver.services;

import java.io.File;

import org.springframework.stereotype.Component;

import be.vito.rma.configtools.common.api.ConfigurationFileService;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
@Component
public class ConfigurationFileServiceImpl implements ConfigurationFileService {

	@Override
	public File getConfigFile() {
		return new File("/etc/marvin/fileserver.properties");
	}

	@Override
	public String getDefaultResourceName() {
		return "fileserver";
	}

	@Override
	public boolean neverUseEnvironmentVariables() {
		return false;	// we can configure the demo server using environment variables
	}

}
