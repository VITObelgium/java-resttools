package be.vito.rma.resttools.tools.progress;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author (c) 2017 Stijn.VanLooy@vito.be
 *
 */
public class ProgressTimerTask extends TimerTask {

	private ProgressProcessHandler process;
	private ProgressListener listener;
	private long target;
	private Timer timer;
	private long delay = 1000;

	public ProgressTimerTask(final ProgressProcessHandler process, final ProgressListener listener,
			final long target, final Timer timer) {
		super();
		this.process = process;
		this.listener = listener;
		this.target = target;
		this.timer = timer;
		// schedule execution in 1000 millis
		try {
			timer.schedule(this, delay);
		} catch (IllegalStateException e) {
			// timer was already canceled: skip rescheduling
		}
	}

	@Override
	public void run() {
		listener.progressUpdated(process.getActual(), target);
		// create new task after this one has finished
		new ProgressTimerTask(process, listener, target, timer);
	}

}
