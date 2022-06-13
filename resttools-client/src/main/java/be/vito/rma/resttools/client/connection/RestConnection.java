package be.vito.rma.resttools.client.connection;

import java.io.File;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.type.TypeReference;

import be.vito.rma.resttools.client.api.ErrorResponseHandler;
import be.vito.rma.resttools.client.api.retry.scheme.RetryScheme;
import be.vito.rma.resttools.client.dtos.Endpoint;
import be.vito.rma.resttools.client.dtos.RequestParameter;
import be.vito.rma.resttools.client.dtos.Response;
import be.vito.rma.resttools.client.exceptions.ErrorResponseException;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import lombok.NonNull;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 * Terminology:
 *  service URL = location where the REST service is hosted
 *  service endpoint = resource within the REST service
 * for example:
 *  a REST service is hosted on https://demo.marvin.vito.be/api (= the service URL)
 *  goodies can be created/retrieved/updated/deleted through
 *   https://demo.marvin.vito.be/api/goodies
 *   ("goodies" is the service endpoint)
 *
 */
public interface RestConnection {

	/**
	 * default is 10 seconds, 0 means wait forever
	 * @param milliseconds
	 */
	public void setConnectTimeout(int milliseconds);

	/**
	 * default is 10 seconds, 0 means wait forever
	 * @param milliseconds
	 */
	public void setReadTimeout(int milliseconds);

	/**
	 * default is 10 seconds, 0 means wait forever
	 * @param milliseconds
	 */
	public void setWriteTimeout(int milliseconds);

	/**
	 * timeout for complete calls
	 * default is 0 (== wait forever)
	 * @param milliseconds
	 */
	public void setCallTimeout(int milliseconds);

	/**
	 * use given proxy for all future requests
	 * note: when using this option, do NOT use the JVM parameters -Dhttp(s).proxyHost -Dhttp(s).proxyPort -Dhttp(s).nonProxyHosts
	 * these will never be overridden by the (un)setProxy method!
	 * if you use both, all requests to the proxy set by setProxy will be sent via the proxy configured by the JVM parameters
	 * (and you probably do not want that)
	 * it is recommended to use the setProxy and not the JVM proxy configuration because when configurated globally
	 * (that is: through JVM parameters) all requests will go through the proxy, also the ones that might originate
	 * from exploits. When using the setProxy method, only requests through this RestConnection instance will "know"
	 * about the proxy. Requests from exploits won't. Using a proxy this way will automatically block
	 * all outgoing requests that originate from exploits.
	 * @param hostname
	 * @param port
	 */
	public void setProxy (@NonNull final String hostname, final int port);

	/**
	 * stop using a proxy
	 */
	public void unsetProxy ();

	/**
	 * Override retry scheme
	 * default is first retry after 10 seconds, give up after a day
	 * @param retryScheme
	 */
	public void setRetryScheme (RetryScheme retryScheme);

	/**
	 * Get the current retry scheme
	 * @return
	 */
	public RetryScheme getRetryScheme ();

	/**
	 * Add given HTTP headers to each request.
	 * Set to null (default) or empty HttpHeaders object to add no headers to the requests.
	 * @param headers
	 */
	public void setRequestHeaders (HttpHeaders headers);

	/**
	 * Create request
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 * @param input
	 */
	public <I, O> O consumePost (Endpoint endpoint, I input, Class<O> outputType,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * Create request
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 * @param input
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <I, O> O consumePost (Endpoint endpoint, I input, Class<O> outputType, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * Create request
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 * @param input
	 */
	public <I, O> O consumePost (Endpoint endpoint, I input, TypeReference<O> outputType,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * Create request
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 * @param input
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <I, O> O consumePost (Endpoint endpoint, I input, TypeReference<O> outputType, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * Retrieve request
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 * @param outputType
	 * @return
	 */
	public <O> O consumeGet (Endpoint endpoint, Class<O> outputType,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * Retrieve request
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 * @param outputType
	 * @return
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <O> O consumeGet (Endpoint endpoint, Class<O> outputType, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * Retrieve request
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 * @param outputType
	 * @return
	 */
	public <O> O consumeGet (Endpoint endpoint, TypeReference<O> outputType,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * Retrieve request
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 * @param outputType
	 * @return
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <O> O consumeGet (Endpoint endpoint, TypeReference<O> outputType, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * update request with no expected output
	 * @param endpoint
	 * @param input
	 */
	public <I> void consumePut (Endpoint endpoint, I input,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * update request with no expected output
	 * @param endpoint
	 * @param input
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <I> void consumePut (Endpoint endpoint, I input, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * update request with an expected output
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 * @param input
	 */
	public <I,O> O consumePut (Endpoint endpoint, I input, Class<O> outputType,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * update request with an expected output
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 * @param input
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <I, O> O consumePut (Endpoint endpoint, I input, Class<O> outputType, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * update request with an expected output
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 * @param input
	 */
	public <I,O> O consumePut (Endpoint endpoint, I input, TypeReference<O> outputType,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * update request with an expected output
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 * @param input
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <I, O> O consumePut (Endpoint endpoint, I input, TypeReference<O> outputType, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * delete request with no expected output
	 * @param endpoint
	 */
	public void consumeDelete (Endpoint endpoint,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * delete request with no expected output
	 * @param endpoint
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public void consumeDelete (Endpoint endpoint, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * delete request with an expected output
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 */
	public <O> O consumeDelete (Endpoint endpoint, Class<O> outputType,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * delete request with an expected output
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <O> O consumeDelete (Endpoint endpoint, Class<O> outputType, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * delete request with an expected output
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 */
	public <O> O consumeDelete (Endpoint endpoint, TypeReference<O> outputType,
			RequestParameter... parameters) throws HttpErrorMessagesException;

	/**
	 * delete request with an expected output
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <O> O consumeDelete (Endpoint endpoint, TypeReference<O> outputType, ErrorResponseHandler errorResponseHandler,
			RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * Custom request, not all HTTP methods might be supported
	 * If your API needs this, it might be useful to consider redesigning your API
	 * This method returns body and response headers, which makes it useful if
	 * the response headers are also needed.
	 * Use this one for simple output types, for example String
	 * @param endpoint
	 * @param method
	 * @param input
	 * @param outputType
	 * @return
	 * @throws HttpErrorMessagesException
	 */
	public <I, O> Response<O> consume (Endpoint endpoint, HttpMethod method, I input, Class<O> outputType,
			RequestParameter... parameters)
		throws HttpErrorMessagesException;

	/**
	 * Custom request, not all HTTP methods might be supported
	 * If your API needs this, it might be useful to consider redesigning your API
	 * This method returns body and response headers, which makes it useful if
	 * the response headers are also needed.
	 * Use this one for composed output types, for example List<String>
	 * @param endpoint
	 * @param method
	 * @param input
	 * @param outputType
	 * @return
	 * @throws HttpErrorMessagesException
	 */
	public <I, O> Response<O> consume (Endpoint endpoint, HttpMethod method, I input, TypeReference<O> outputType,
			RequestParameter... parameters)
		throws HttpErrorMessagesException;

	/**
	 * Same as above, but with own error response handler instead of expecting ErrorMessages in the response
	 * and throwing an HttpErrorMessagesException when receiving them.
	 * @param endpoint
	 * @param method
	 * @param input
	 * @param outputType
	 * @param errorResponseHandler
	 * @param parameters
	 * @return
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <I, O> Response<O> consume (Endpoint endpoint, HttpMethod method, I input, Class<O> outputType,
			ErrorResponseHandler errorResponseHandler, RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * Same as above, but with own error response handler instead of expecting ErrorMessages in the response
	 * and throwing an HttpErrorMessagesException when receiving them.
	 * @param endpoint
	 * @param method
	 * @param input
	 * @param outputType
	 * @param errorResponseHandler
	 * @param parameters
	 * @return
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called (the method never returns null)
	 */
	public <I, O> Response<O> consume (Endpoint endpoint, HttpMethod method, I input, TypeReference<O> outputType,
			ErrorResponseHandler errorResponseHandler, RequestParameter... parameters) throws ErrorResponseException;

	/**
	 * File download request, not all HTTP methods support input payloads (for example: the GET method
	 * requires a null body), in that case use a VoidType instance as input
	 * @param endpoint
	 * @param method
	 * @param input
	 * @param targetFile the location into which the downloaded file must be saved
	 * @param parameters
	 * @throws HttpErrorMessagesException
	 */
	public <I> void downloadFile (Endpoint endpoint, HttpMethod method, I input, File targetFile,
			RequestParameter... parameters)
		throws HttpErrorMessagesException;

	/**
	 * Same as above, but with own error response handler instead of expecting ErrorMessages in the response
	 * and throwing an HttpErrorMessagesException when receiving them.
	 * @param endpoint
	 * @param method
	 * @param input
	 * @param targetFile
	 * @param errorResponseHandler
	 * @param parameters
	 * @throws ErrorResponseException if an error response was received and the given ErrorResponseHandler was called
	 */
	public <I> void downloadFile (final Endpoint endpoint, final HttpMethod method, final I input, final File targetFile,
			ErrorResponseHandler errorResponseHandler, RequestParameter... parameters) throws ErrorResponseException;

	// TODO: add support for progress listeners and cancellation in file uploads/downloads

}
