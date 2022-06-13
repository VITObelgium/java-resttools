package be.vito.rma.resttools.server.dtos;

import java.io.File;

import lombok.Getter;
import lombok.NonNull;

/**
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
public final class FileWrapper {

	@Getter private final File file;
	@Getter private final String reportedFilename;

	/**
	 *
	 * @param file
	 * @param reportedFilename filename to use in the content-disposition header (you might want to hide the filename on the server from the user)
	 */
	public FileWrapper(@NonNull final File file, @NonNull final String reportedFilename) {
		this.file = file;
		this.reportedFilename = reportedFilename;
	}

	public FileWrapper(final File file) {
		this(file, file.getName());
	}

}
