package be.vito.rma.resttools.client.connection;

import static be.vito.rma.resttools.json.JsonTools.toPrettyJson;

import java.io.File;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;

import be.vito.rma.resttools.api.dtos.Healthcheck;
import be.vito.rma.resttools.api.dtos.Version;
import be.vito.rma.resttools.api.dtos.VoidType;
import be.vito.rma.resttools.client.api.ErrorResponseHandler;
import be.vito.rma.resttools.client.api.retry.scheme.RetryScheme;
import be.vito.rma.resttools.client.dtos.Endpoint;
import be.vito.rma.resttools.client.dtos.FormBody;
import be.vito.rma.resttools.client.dtos.MultiPartBody;
import be.vito.rma.resttools.client.dtos.RequestParameter;
import be.vito.rma.resttools.client.dtos.Response;
import be.vito.rma.resttools.client.exceptions.ErrorResponseException;
import be.vito.rma.resttools.client.tools.UrlTools;
import be.vito.rma.resttools.errors.enums.ResttoolsErrors;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;

/**
 * @author (c) 2016-2020 Stijn.VanLooy@vito.be
 *
 */
@Slf4j
public abstract class AbstractRestConnection implements RestConnection {

	private final RawConnection rawConnection = new RawConnection(this);
	private String serviceUrl;

	@Setter private HttpHeaders requestHeaders;

	public AbstractRestConnection(final String serviceUrl) throws HttpErrorMessagesException {
		this(serviceUrl, null);
	}

	public AbstractRestConnection(final String serviceUrl, final String requiredVersionPrefix) throws HttpErrorMessagesException {
		super();
		this.serviceUrl = serviceUrl;
		// cut of trailing "/" from serviceUrl
		while (this.serviceUrl.endsWith("/"))
			this.serviceUrl = this.serviceUrl.substring(0, this.serviceUrl.length() - 1);

		if (requiredVersionPrefix != null) {
			try {
				final DefaultRestConnection checkConn = new DefaultRestConnection(serviceUrl);
				// do healthcheck
				checkConn.consumeGet(new Endpoint("healthcheck"), Healthcheck.class);

				// check version
				final Version version = checkConn.consumeGet(new Endpoint("version"), Version.class);
				if (!version.getVersion().startsWith(requiredVersionPrefix))
					throw ResttoolsErrors.UNSUPPORTED_VERSION.getException(version.getVersion(), requiredVersionPrefix);

				if (version.getVersion().toUpperCase().contains("SNAPSHOT"))
					log.warn("Using a snapshot release deployed @ " + serviceUrl + " (" + version.getVersion() + ")");
			} catch (final HttpErrorMessagesException e) {
				e.addMessage(ResttoolsErrors.CONNECTION_FAILED.getMessage(serviceUrl));
				throw e;
			}
		}

	}

	@Override
	public void setConnectTimeout(final int milliseconds) {
		rawConnection.setConnectTimeout(milliseconds);
	}

	@Override
	public void setReadTimeout(final int milliseconds) {
		rawConnection.setReadTimeout(milliseconds);
	}

	@Override
	public void setWriteTimeout(final int milliseconds) {
		rawConnection.setWriteTimeout(milliseconds);
	}

	@Override
	public void setCallTimeout(final int milliseconds) {
		rawConnection.setCallTimeout(milliseconds);
	}

	@Override
	public void setProxy (@NonNull final String hostname, final int port) {
		rawConnection.setProxy(hostname, port);
	}

	@Override
	public void unsetProxy () {
		rawConnection.unsetProxy();
	}

	@Override
	public void setRetryScheme (final RetryScheme retryScheme) {
		rawConnection.setRetryScheme(retryScheme);
	}

	@Override
	public RetryScheme getRetryScheme () {
		return rawConnection.getRetryScheme();
	}

	@Override
	public <I, O> O consumePost(final Endpoint endpoint, final I input, final Class<O> outputType,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		return consume(endpoint, HttpMethod.POST, input, outputType, parameters).getBody();
	}

	@Override
	public <I, O> O consumePost(final Endpoint endpoint, final I input, final Class<O> outputType,
			final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		return consume(endpoint, HttpMethod.POST, input, outputType, errorResponseHandler, parameters).getBody();
	}

	@Override
	public <I, O> O consumePost(final Endpoint endpoint, final I input, final TypeReference<O> outputType,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		return consume(endpoint, HttpMethod.POST, input, outputType, parameters).getBody();
	}

	@Override
	public <I, O> O consumePost(final Endpoint endpoint, final I input, final TypeReference<O> outputType,
			final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		return consume(endpoint, HttpMethod.POST, input, outputType, errorResponseHandler, parameters).getBody();
	}

	@Override
	public <O> O consumeGet(final Endpoint endpoint, final Class<O> outputType,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		return consume(endpoint, HttpMethod.GET, new VoidType(), outputType, parameters).getBody();
	}

	@Override
	public <O> O consumeGet(final Endpoint endpoint, final Class<O> outputType,
			final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		return consume(endpoint, HttpMethod.GET, new VoidType(), outputType, errorResponseHandler, parameters).getBody();
	}

	@Override
	public <O> O consumeGet(final Endpoint endpoint, final TypeReference<O> outputType,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		return consume(endpoint, HttpMethod.GET, new VoidType(), outputType, parameters)
				.getBody();
	}

	@Override
	public <O> O consumeGet(final Endpoint endpoint, final TypeReference<O> outputType,
			final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		return consume(endpoint, HttpMethod.GET, new VoidType(), outputType, errorResponseHandler, parameters).getBody();
	}

	@Override
	public <I> void consumePut(final Endpoint endpoint, final I input,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		consume(endpoint, HttpMethod.PUT, input, VoidType.class, parameters);
	}

	@Override
	public <I> void consumePut(final Endpoint endpoint, final I input,
			final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		consume(endpoint, HttpMethod.PUT, input, VoidType.class, errorResponseHandler, parameters);
	}

	@Override
	public <I,O> O consumePut (final Endpoint endpoint, final I input, final Class<O> outputType,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		return consume(endpoint, HttpMethod.PUT, input, outputType, parameters).getBody();
	}

	@Override
	public <I, O> O consumePut (final Endpoint endpoint, final I input, final Class<O> outputType, final ErrorResponseHandler errorResponseHandler,
			final RequestParameter... parameters) throws ErrorResponseException {
		return consume(endpoint, HttpMethod.PUT, input, outputType, errorResponseHandler, parameters).getBody();
	}

	@Override
	public <I,O> O consumePut (final Endpoint endpoint, final I input, final TypeReference<O> outputType,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		return consume(endpoint, HttpMethod.PUT, input, outputType, parameters).getBody();
	}

	@Override
	public <I, O> O consumePut (final Endpoint endpoint, final I input, final TypeReference<O> outputType, final ErrorResponseHandler errorResponseHandler,
			final RequestParameter... parameters) throws ErrorResponseException {
		return consume(endpoint, HttpMethod.PUT, input, outputType, errorResponseHandler, parameters).getBody();
	}

	@Override
	public void consumeDelete(final Endpoint endpoint,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		consume(endpoint, HttpMethod.DELETE, new VoidType(), VoidType.class, parameters);
	}

	@Override
	public void consumeDelete(final Endpoint endpoint, final ErrorResponseHandler errorResponseHandler,
			final RequestParameter... parameters) throws ErrorResponseException {
		consume(endpoint, HttpMethod.DELETE, new VoidType(), VoidType.class, errorResponseHandler, parameters);
	}

	@Override
	public <O> O consumeDelete (final Endpoint endpoint, final Class<O> outputType,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		return consume(endpoint, HttpMethod.DELETE, new VoidType(), outputType, parameters).getBody();
	}

	@Override
	public <O> O consumeDelete (final Endpoint endpoint, final Class<O> outputType, final ErrorResponseHandler errorResponseHandler,
			final RequestParameter... parameters) throws ErrorResponseException {
		return consume(endpoint, HttpMethod.DELETE, new VoidType(), outputType, errorResponseHandler, parameters).getBody();
	}

	@Override
	public <O> O consumeDelete (final Endpoint endpoint, final TypeReference<O> outputType,
			final RequestParameter... parameters) throws HttpErrorMessagesException {
		return consume(endpoint, HttpMethod.DELETE, new VoidType(), outputType, parameters).getBody();
	}

	@Override
	public <O> O consumeDelete (final Endpoint endpoint, final TypeReference<O> outputType, final ErrorResponseHandler errorResponseHandler,
			final RequestParameter... parameters) throws ErrorResponseException {
		return consume(endpoint, HttpMethod.DELETE, new VoidType(), outputType, errorResponseHandler, parameters).getBody();
	}

	private <I> Object createBody (final I input, final HttpHeaders headers) {
		if (input instanceof MultiPartBody) {
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			// no body parsing/transforming required
			return input;
		} else if (input instanceof File) {
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentLength(((File) input).length());
			// no body parsing/transforming required
			return input;
		} else if (input instanceof FormBody) {
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			return ((FormBody)input).createBody();
		} else if (!(input instanceof VoidType)) {
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			return toPrettyJson(input);
		}

		// VoidType body
		return null;
	}

	private HttpUrl createUrlWithoutParameters (final Endpoint endpoint) {
		return UrlTools.createUrl(serviceUrl, endpoint);
	}

	// TODO: how to avoid code duplication here?

	@Override
	public <I, O> Response<O> consume(final Endpoint endpoint, final HttpMethod method,
			final I input, final Class<O> outputType, final RequestParameter... parameters)
			throws HttpErrorMessagesException {
		final HttpHeaders headers = createAllRequestHeaders();
		final Object body = createBody(input, headers);
		final HttpUrl url = createUrlWithoutParameters(endpoint);
		final Response<O> out = rawConnection.doRawRequest(url, method, body, headers, outputType, parameters);
		handleResponseHeaders(out.getHeaders(), out.getRequestUrl());
		return out;
	}

	@Override
	public <I, O> Response<O> consume(final Endpoint endpoint, final HttpMethod method,
			final I input, final TypeReference<O> outputType, final RequestParameter... parameters)
			throws HttpErrorMessagesException {
		final HttpHeaders headers = createAllRequestHeaders();
		final Object body = createBody(input, headers);
		final HttpUrl url = createUrlWithoutParameters(endpoint);
		final Response<O> out = rawConnection.doRawRequest(url, method, body, headers, outputType, parameters);
		handleResponseHeaders(out.getHeaders(), out.getRequestUrl());
		return out;
	}

	// TODO: how to avoid code duplication here?

	@Override
	public <I, O> Response<O> consume (final Endpoint endpoint, final HttpMethod method, final I input, final Class<O> outputType,
			final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		final HttpHeaders headers = createAllRequestHeaders();
		final Object body = createBody(input, headers);
		final HttpUrl url = createUrlWithoutParameters(endpoint);
		final Response<O> out = rawConnection.doRawRequest(url, method, body, headers, outputType, errorResponseHandler, parameters);
		handleResponseHeaders(out.getHeaders(), out.getRequestUrl());
		return out;
	}

	@Override
	public <I, O> Response<O> consume (final Endpoint endpoint, final HttpMethod method, final I input, final TypeReference<O> outputType,
			final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		final HttpHeaders headers = createAllRequestHeaders();
		final Object body = createBody(input, headers);
		final HttpUrl url = createUrlWithoutParameters(endpoint);
		final Response<O> out = rawConnection.doRawRequest(url, method, body, headers, outputType, errorResponseHandler, parameters);
		handleResponseHeaders(out.getHeaders(), out.getRequestUrl());
		return out;
	}

	@Override
	public <I> void downloadFile (final Endpoint endpoint, final HttpMethod method, final I input, final File targetFile,
			final RequestParameter... parameters)
		throws HttpErrorMessagesException {
		final HttpHeaders headers = createAllRequestHeaders();
		final Object body = createBody(input, headers);
		final HttpUrl url = createUrlWithoutParameters(endpoint);
		final Response<String> out = rawConnection.downloadFile(url, method, body, headers, targetFile, parameters);
		handleResponseHeaders(out.getHeaders(), out.getRequestUrl());
	}

	@Override
	public <I> void downloadFile (final Endpoint endpoint, final HttpMethod method, final I input, final File targetFile,
			final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		final HttpHeaders headers = createAllRequestHeaders();
		final Object body = createBody(input, headers);
		final HttpUrl url = createUrlWithoutParameters(endpoint);
		final Response<String> out = rawConnection.downloadFile(url, method, body, headers, targetFile, errorResponseHandler, parameters);
		handleResponseHeaders(out.getHeaders(), out.getRequestUrl());
	}

	public HttpHeaders createAllRequestHeaders () {
		final HttpHeaders headers = createRequestHeaders();
		if (requestHeaders != null && !requestHeaders.isEmpty()) {
			for (final String key : requestHeaders.keySet()) {
				headers.addAll(key, requestHeaders.get(key));
			}
		}
		return headers;
	}

	protected abstract HttpHeaders createRequestHeaders ();

	protected abstract void handleResponseHeaders (HttpHeaders headers, String requestUrl);

}
