package be.vito.rma.resttools.api.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NonNull;

/**
 * @author (c) 2021-2022 Stijn.VanLooy@vito.be
 *
 */
public class DeploymentUrl {

	@Getter private final String schema;
	@Getter private final String host;
	@Getter private final String port;
	@Getter private final String prefix;

	// enables jackson to deserialize json into a DeploymentUrl instance
	protected DeploymentUrl() {
		schema = null;
		host = null;
		port = null;
		prefix = null;
	}

	public DeploymentUrl (@NonNull final String schema, @NonNull final String host, final String port, @NonNull final String prefix) {
		super();
		this.schema = schema;
		this.host = host;
		this.port = port;
		this.prefix = prefix;
	}

	@JsonIgnore
	public String getUrl () {
		final StringBuilder sb = new StringBuilder();
		sb.append(getSchema());
		sb.append("://");
		sb.append(getHostWithOptionalPort());
		sb.append("/");
		sb.append(getPrefix());
		String url = sb.toString();
		if (!url.endsWith("/")) url = url + "/";
		return url;
	}

	@JsonIgnore
	public String getHostWithOptionalPort () {
		if (getPort() == null) {
			return getHost();
		} else {
			return getHost() + ":" + getPort();
		}
	}

	@Override
	public String toString() {
		return getUrl();
	}

}
