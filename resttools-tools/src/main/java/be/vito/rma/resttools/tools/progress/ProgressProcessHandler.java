package be.vito.rma.resttools.tools.progress;

import java.util.Timer;

import lombok.Getter;

/**
 * @author (c) 2017 Stijn.VanLooy@vito.be
 *
 */
public class ProgressProcessHandler {

	@Getter private long actual = 0;

	public void add2actual (final long value) {
		actual += value;
	}

	public void run (final Runnable runnable, final ProgressListener listener, final long expectedTarget) {
		if (listener == null) {
			// no progress reporting
			runnable.run();
			return;
		}
		// reset actual value
		actual = 0;
		// Create timer for notifying the progress listener
		Timer timer = new Timer();
		// create the first progress task: this will schedule it as well
		new ProgressTimerTask(this, listener, expectedTarget, timer);
		try {
			runnable.run();
		} catch (Exception e) {
			listener.failed(getActual());
			throw new RuntimeException(e);
		} finally {
			timer.cancel();
		}
		listener.completed(getActual());
	}


}
