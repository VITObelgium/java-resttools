package be.vito.rma.resttools.server.api;

import java.io.File;

import be.vito.rma.resttools.api.dtos.VoidType;

/**
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
public interface FileRequestHandler<O> extends RequestHandler<VoidType, O> {

	public File getTargetFile();

}
