package be.vito.rma.resttools.client.api.retry.scheme;

import be.vito.rma.resttools.client.api.retry.filter.DefaultErrorResponseRetryFilter;
import be.vito.rma.resttools.client.api.retry.filter.ErrorResponseRetryFilter;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * When consuming a REST endpoint fails, retrying will start after a given amount of time.
 * The wait interval before retrying will be doubled from then on, to avoid overloading
 * an already busy server/network.
 * Retrying will be given up after a given amount of time. After that time the error exception
 * will be propagated to the application.
 *
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
public class RetryScheme {

	/**
	 * The filter to use to determine whether a retry is needed upon a given (error) response
	 */
	@Getter @Setter private ErrorResponseRetryFilter errorResponseRetryFilter = new DefaultErrorResponseRetryFilter();

	/**
	 * Set default first retry (seconds) for RetrySchemes
	 * (default = 10 seconds)
	 */
	@Setter private static int defaultFirstRetry = 10;
	/**
	 * Set default give up after (seconds) for RetrySchemes
	 * (default = 1 day)
	 */
	@Setter private static int defaultGiveUpAfter = 3600 * 24;

	/**
	 * Set to true to skip waiting for the next retry
	 */
	@Setter private boolean nextRetryWithoutWaiting = false;

	public static void setDefaults (final int defaultFirstRetry, final int defaultGiveUpAfter) {
		setDefaultFirstRetry(defaultFirstRetry);
		setDefaultGiveUpAfter(defaultGiveUpAfter);
	}

	/**
	 * Use first retry and give up after values from given RetryScheme
	 * as default values for those parameters
	 * @param other
	 */
	public static void setDefaults (final RetryScheme other) {
		setDefaultFirstRetry(other.firstRetry);
		setDefaultGiveUpAfter(other.giveUpAfter);
	}

	private final int firstRetry, giveUpAfter;

	/**
	 * Default retry scheme: use default first retry and give up after values
	 * This retry scheme is used by "internal" connections (used to do health and version checks, etc.)
	 */
	public RetryScheme () {
		this (defaultFirstRetry, defaultGiveUpAfter);
	}

	/**
	 *
	 * @param firstRetry begin retrying after this many seconds after the failure
	 * @param giveUpAfter give up retrying after this many seconds after the failure
	 */
	public RetryScheme (final int firstRetry, final int giveUpAfter) {
		if (firstRetry <= 0 || giveUpAfter <= firstRetry)
			throw new RuntimeException("firstRetry must be > 0, giveUpAfter must be > firstRetry");
		this.firstRetry = firstRetry;
		this.giveUpAfter = giveUpAfter;
	}

	/**
	 * Calculates the amount of milliseconds to wait given this retry scheme
	 * if the first request was send at the given time
	 * @param startMilliseconds return value of System.currentTimeMillis() when the first request was send
	 * @return amount of milliseconds to wait, -1 if it is time to give up trying
	 */
	public long getWaitMilliseconds (final long startMilliseconds) {
		if (nextRetryWithoutWaiting) {
			nextRetryWithoutWaiting = false;
			return 0;	// don't wait in this exceptional case
		}
		final long currentMilliseconds = System.currentTimeMillis();
		if (currentMilliseconds - startMilliseconds > giveUpAfter * 1000)
			return -1;	// waited long enough
		long out = firstRetry * 1000;
		long totalWaiting = 0;
		while (startMilliseconds + totalWaiting < currentMilliseconds) {
			totalWaiting += out;
			out *= 2;
		}
		return out > firstRetry * 1000 ? out / 2 : out;
	}

}
