package be.vito.rma.resttools.client.api.retry.scheme;

/**
 *
 *  A retry scheme that never retries
 *
 * @author (c) 2018 Stijn.VanLooy@vito.be
 *
 */
public class NoRetryScheme extends RetryScheme {

	@Override
	public long getWaitMilliseconds (final long startMilliseconds) {
		return -1;
	}

}
