package be.vito.rma.resttools.tools.progress;

/**
 * @author (c) 2017 Stijn.VanLooy@vito.be
 *
 */
public interface ProgressListener {

	/**
	 * Called when progress has changed
	 * progress in % = actual / target * 100
	 * @param actual actual progress value
	 * @param target estimated target progress value
	 */
	public void progressUpdated (long actual, long target);

	/**
	 * Called after the process has completed successfully
	 * @param actual
	 */
	public void completed(long actual);

	/**
	 * Called if the process failed
	 * @param actual
	 */
	public void failed (long actual);

}
