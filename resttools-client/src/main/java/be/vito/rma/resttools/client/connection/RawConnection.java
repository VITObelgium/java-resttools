package be.vito.rma.resttools.client.connection;

import static be.vito.rma.resttools.json.JsonTools.fromJson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;

import be.vito.rma.resttools.client.api.BodyParser;
import be.vito.rma.resttools.client.api.ErrorResponseHandler;
import be.vito.rma.resttools.client.api.HttpErrorMessagesExceptionWrapper;
import be.vito.rma.resttools.client.api.WrappingErrorResponseHandler;
import be.vito.rma.resttools.client.api.retry.filter.ErrorResponseRetryFilter;
import be.vito.rma.resttools.client.api.retry.scheme.RetryScheme;
import be.vito.rma.resttools.client.dtos.Endpoint;
import be.vito.rma.resttools.client.dtos.MultiPartBody;
import be.vito.rma.resttools.client.dtos.RequestParameter;
import be.vito.rma.resttools.client.dtos.Response;
import be.vito.rma.resttools.client.exceptions.ErrorResponseException;
import be.vito.rma.resttools.client.tools.UrlTools;
import be.vito.rma.resttools.errors.exceptions.HttpErrorMessagesException;
import be.vito.rma.resttools.tools.StreamTools;
import be.vito.rma.resttools.tools.StringTools;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author (c) 2016-2020 Stijn.VanLooy@vito.be
 *
 */
@Slf4j
public class RawConnection {

	/*
	 * only use while testing with self-signed certificates
	 * NEVER use in production!
	 */
	@Getter @Setter private static boolean skipHttpsCertificateValidation = false;

	@Getter @Setter private int connectTimeout = 10 * 1000; // default = 10 seconds
	@Getter @Setter private int readTimeout = 10 * 1000; // default = 10 seconds
	@Getter @Setter private int writeTimeout = 10 * 1000; // default = 10 seconds
	@Getter @Setter private int callTimeout = 0; // default = no timeout
	@Getter @Setter private RetryScheme retryScheme = new RetryScheme();

	private Proxy proxy = null;

	private OkHttpClient okHttpClient = new OkHttpClient();

	private AbstractRestConnection parentConnection;

	public RawConnection () {
		this(null);
	}
	/**
	 *
	 * @param parentConnection if given, request headers are refreshed using the parent connection when retrying
	 */
	public RawConnection (final AbstractRestConnection parentConnection) {
		super();

		this.parentConnection = parentConnection;

		if (skipHttpsCertificateValidation) {
			/*
			 * src: http://stackoverflow.com/a/876785
			 */
			// Create a trust manager that does not validate certificate chains
		    final TrustManager[] trustAllCerts = new TrustManager[] {
		      new X509TrustManager() {
		        @Override
				public X509Certificate[] getAcceptedIssuers() {
		          return new X509Certificate[0];
		        }
		        @Override
				public void checkClientTrusted(final X509Certificate[] certs, final String authType) {}
		        @Override
				public void checkServerTrusted(final X509Certificate[] certs, final String authType) {}
		    }};

		    // Ignore differences between given hostname and certificate hostname
		    final HostnameVerifier hv = new HostnameVerifier() {
		      @Override
			public boolean verify(final String hostname, final SSLSession session) { return true; }
		    };

		    // Install the all-trusting trust manager
		    try {
		      final SSLContext sc = SSLContext.getInstance("SSL");
		      sc.init(null, trustAllCerts, new SecureRandom());
		      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		      HttpsURLConnection.setDefaultHostnameVerifier(hv);
		    } catch (NoSuchAlgorithmException | KeyManagementException e) {
		    	throw new RuntimeException(e);
		    }
		}
	}

	public void setProxy (@NonNull final String hostname, final int port) {
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.marvin.vito.local", 3128));
	}

	public void unsetProxy () {
		proxy = null;
	}

	// TODO: how to avoid code duplication here?

	public <I, O> Response<O> doRawRequest (final HttpUrl url, final HttpMethod method,final I body, final HttpHeaders headers,
			final Class<O> outputType, final RequestParameter... parameters) throws HttpErrorMessagesException {
		final HttpErrorMessagesExceptionWrapper exceptionWrapper = new HttpErrorMessagesExceptionWrapper();
		try {
			return doRawRequest(url, method, body, headers, outputType, new WrappingErrorResponseHandler(exceptionWrapper), parameters);
		} catch (final ErrorResponseException e) {
			throw exceptionWrapper.getException();
		}
	}

	public <I, O> Response<O> doRawRequest(final HttpUrl url, final HttpMethod method, final I body, final HttpHeaders headers,
			final TypeReference<O> outputType, final RequestParameter... parameters) throws HttpErrorMessagesException {
		final HttpErrorMessagesExceptionWrapper exceptionWrapper = new HttpErrorMessagesExceptionWrapper();
		try {
			return doRawRequest(url, method, body, headers, outputType, new WrappingErrorResponseHandler(exceptionWrapper), parameters);
		} catch (final ErrorResponseException e) {
			throw exceptionWrapper.getException();
		}
	}

	// TODO: how to avoid code duplication here?

	public <I, O> Response<O> doRawRequest (final HttpUrl url, final HttpMethod method,final I body, final HttpHeaders headers,
			final Class<O> outputType, final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters)
					throws ErrorResponseException {
		return doRawRequest(url, method, headers, body, new BodyParser<O>() {
			@SuppressWarnings("unchecked")
			@Override
			public void parseBody(final String body, final Response<O> response) {
				// if output type is a String, don't use the JSON parser
				if (outputType == String.class)
					response.setBody((O) body);
				else
					response.setBody(fromJson(body, outputType));
			}
			@Override
			public File getTargetFile() {
				return null;
			}
		}, errorResponseHandler, parameters);
	}

	public <I, O> Response<O> doRawRequest(final HttpUrl url, final HttpMethod method, final I body, final HttpHeaders headers,
			final TypeReference<O> outputType, final ErrorResponseHandler errorResponseHandler, final RequestParameter... parameters) throws ErrorResponseException {
		return doRawRequest(url, method, headers, body, new BodyParser<O>() {
			@Override
			public void parseBody(final String body, final Response<O> response) {
				response.setBody(fromJson(body, outputType));
			}
			@Override
			public File getTargetFile() {
				return null;
			}
		}, errorResponseHandler, parameters);
	}

	public <I> Response<String> downloadFile (final HttpUrl url, final HttpMethod method,final I body, final HttpHeaders headers,
			final File targetFile, final RequestParameter... parameters) throws HttpErrorMessagesException {
		final HttpErrorMessagesExceptionWrapper exceptionWrapper = new HttpErrorMessagesExceptionWrapper();
		try {
			return downloadFile(url, method, body, headers, targetFile, new WrappingErrorResponseHandler(exceptionWrapper), parameters);
		} catch (final ErrorResponseException e) {
			throw exceptionWrapper.getException();
		}
	}

	public <I> Response<String> downloadFile (final HttpUrl url, final HttpMethod method,final I body, final HttpHeaders headers,
			final File targetFile, final ErrorResponseHandler errorResponseHandler,
			final RequestParameter... parameters) throws ErrorResponseException {
		return doRawRequest(url, method, headers, body, new BodyParser<String>() {
			@Override
			public void parseBody(final String body, final Response<String> response) {
				throw new RuntimeException("The body of a file download cannot be parsed.");
			}
			@Override
			public File getTargetFile() {
				return targetFile;
			}
		}, errorResponseHandler, parameters);

	}


	// FIXME: add enforced minimum amount of time between requests

	private <I, O> Response<O> doRawRequest (final HttpUrl url, final HttpMethod method, HttpHeaders headers,
			final I body, final BodyParser<O> bodyParser, final ErrorResponseHandler errorResponseHandler,
			final RequestParameter... parameters) throws ErrorResponseException {
		// only retry code here, see executeRawRequest for actual request execution
		// (redirection is handled by the Spring framework)
		// when (not) to retry based upon http://resthooks.org/docs/retries/
		// FIXME: implement Retry-After header support: https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.37
		final long startMilliseconds = System.currentTimeMillis();
		final ErrorResponseCache errorResponseCache = new ErrorResponseCache(errorResponseHandler);
		while (true) {
			try {
				return executeRawRequest(url, method, headers, body, bodyParser, errorResponseCache, parameters);
			} catch (final ErrorResponseException e) {
				// retry if the retry filter returns true
				boolean retry = errorResponseCache.checkRetryFilter(getRetryScheme().getErrorResponseRetryFilter());
				long waitMilliseconds = 0;
				if (retry) {
					waitMilliseconds = getRetryScheme().getWaitMilliseconds(startMilliseconds);
					// only retry if returned wait milliseconds are non-negative
					retry = waitMilliseconds >= 0;
				}
				if (!retry) {
					// give up retrying
					errorResponseCache.flush();
					throw new ErrorResponseException();
				} else {
					// log, wait, retry
					if (log.isWarnEnabled()) {
						final Integer statusCode = errorResponseCache.getStatusCodeValue();
						final String statusPhrase = errorResponseCache.getStatusCodePhrase();
						final StringBuilder sb = new StringBuilder();
						sb.append("Request to "); sb.append(errorResponseCache.getRequestUrl()); sb.append(" failed: ");
						if (statusCode != null) {
							sb.append(statusCode);
							sb.append(" ");
						}
						if (statusPhrase != null) {
							sb.append(statusPhrase);
							sb.append(" ");
						}
						sb.append("(retrying in "); sb.append(waitMilliseconds / 1000);	sb.append(" seconds)");
						log.warn(sb.toString());
					}
					try {
						Thread.sleep(waitMilliseconds);
					} catch (final InterruptedException ie) {
						throw new RuntimeException("Interrupted while waiting to retry", ie);
					}
					// refresh headers before retrying (for example the access token might have been updated)
					headers = parentConnection.createAllRequestHeaders();
				}
			}
		}
	}

	/*
	 * Inspired by
	 *  the method public void doWithRequest(ClientHttpRequest httpRequest) throws IOException
	 * 	in the private class HttpEntityRequestCallback
	 *  from org.springframework.web.client.RestTemplate
	 * See:
	 *  https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/main/java/org/springframework/web/client/RestTemplate.java
	 * This method isn't accessible, so this part of the code might stop working with future Spring versions :-/
	 * TODO: figure out a less hacky way to use the "Spring magic" to create request body contents
	 */
	@SuppressWarnings("unchecked")
	private void streamRequestBody2HttpRequest (final Object requestBody, final HttpOutputMessage httpRequest, final MediaType requestContentType, final RestTemplate restTemplate) throws HttpMessageNotWritableException, IOException {
		final Class<?> requestBodyClass = requestBody.getClass();
		final Type requestBodyType = requestBodyClass;
		for (final HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
			if (messageConverter instanceof GenericHttpMessageConverter) {
				final GenericHttpMessageConverter<Object> genericConverter =
						(GenericHttpMessageConverter<Object>) messageConverter;
				if (genericConverter.canWrite(requestBodyType, requestBodyClass, requestContentType)) {
					genericConverter.write(requestBody, requestBodyType, requestContentType, httpRequest);
					return;
				}
			}
			else if (messageConverter.canWrite(requestBodyClass, requestContentType)) {
				((HttpMessageConverter<Object>) messageConverter).write(
						requestBody, requestContentType, httpRequest);
				return;
			}
		}
		String message = "Could not write request: no suitable HttpMessageConverter found for request type [" +
				requestBodyClass.getName() + "]";
		if (requestContentType != null) {
			message += " and content type [" + requestContentType + "]";
		}
		throw new RestClientException(message);
	}

	/*
	 * Convert request body to a String suitable for logging
	 * and as close as possible to the data that was/will be sent
	 */
	private String requestBody2String (final Object requestBody, final HttpHeaders headers) {
		if (requestBody == null) return null;
		// we want the request body in a String, not in an HttpOutputMessage
		// -> create custom HttpOutputMessage instance to do that
		final HttpOutputMessage hom = new HttpOutputMessage() {

			private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			@Override
			public HttpHeaders getHeaders() {
				return headers;
			}

			@Override
			public OutputStream getBody() throws IOException {
				return baos;
			}

			@Override
			public String toString() {
				return new String(baos.toByteArray());
			}
		};
		// we don't want to dump the contents of files in multipart bodies into loggings
		// -> create RestTemplate with a modified HttpMessageConverter for that purpose
		// -> the file location will be written to the loggings now
		final RestTemplate restTemplate = new RestTemplate();
		for (final HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
			if (converter instanceof AllEncompassingFormHttpMessageConverter) {
				final AllEncompassingFormHttpMessageConverter formConverter = (AllEncompassingFormHttpMessageConverter) converter;
				// we know that our multipart bodies only contains Strings or FileSystemResources
				// (all other types are already serialized)
				final List<HttpMessageConverter<?>> partConverters = new ArrayList<>();
				partConverters.add(new StringHttpMessageConverter());
				partConverters.add(new HttpMessageConverter<FileSystemResource>() {
					@Override
					public boolean canRead(final Class<?> clazz, final MediaType mediaType) {
						throw new UnsupportedOperationException("Not implemented");
					}

					@Override
					public boolean canWrite(final Class<?> clazz, final MediaType mediaType) {
						return clazz == FileSystemResource.class;
					}

					@Override
					public List<MediaType> getSupportedMediaTypes() {
						throw new UnsupportedOperationException("Not implemented");
					}

					@Override
					public FileSystemResource read(final Class<? extends FileSystemResource> clazz, final HttpInputMessage inputMessage)
							throws IOException, HttpMessageNotReadableException {
						throw new UnsupportedOperationException("Not implemented");
					}

					@Override
					public void write(final FileSystemResource t, final MediaType contentType, final HttpOutputMessage outputMessage)
							throws IOException, HttpMessageNotWritableException {
						outputMessage.getBody().write(t.toString().getBytes());
					}
				});
				formConverter.setPartConverters(partConverters);
			}
		}
		try {
			streamRequestBody2HttpRequest(requestBody, hom, headers.getContentType(), restTemplate);
		} catch (final Exception e) {
			// we don't want some RuntimeException to interfere with our trace logging
			final String message = "Failed serializing request body";
			log.error(message, e);
			return message;
		}
		return hom.toString();
	}

	private <I, O> Response<O> executeRawRequest (final HttpUrl url, final HttpMethod method, final HttpHeaders headers,
			final I body, final BodyParser<O> bodyParser, final ErrorResponseHandler errorResponseHandler,
			final RequestParameter... parameters) throws ErrorResponseException {

		// create the url
		final HttpUrl httpUrl = UrlTools.createUrl(url, new Endpoint(), parameters);
		final String loggingRequestUrl = httpUrl.toString();

		// create the request body
		final okhttp3.MediaType mediaType = okhttp3.MediaType.parse(
				(headers.getContentType() == null ? MediaType.APPLICATION_JSON : headers.getContentType())
				.getType());
		RequestBody requestBody = null;
		if (body == null) {
			requestBody = null;
			if (method == HttpMethod.POST || method == HttpMethod.PUT)	// POST and PUT requests must have a body
				requestBody = RequestBody.create("", okhttp3.MediaType.parse(MediaType.TEXT_PLAIN.getType()));
		} else if (body instanceof File) {
			final File file = (File) body;
			requestBody = RequestBody.create(file, mediaType);
		} else if (body instanceof String) {
			// json content is already serialized into a String at this point
			final String string = (String) body;
			requestBody = RequestBody.create(string, mediaType);
		} else if (body instanceof MultiPartBody) {
			final MultiPartBody mpb = (MultiPartBody) body;
			MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			for (final String key : mpb.keySet()) {
				for (final Object value : mpb.get(key)) {
					if (value instanceof FileSystemResource) {
						final FileSystemResource fsr = (FileSystemResource) value;
						final File file = fsr.getFile();
						builder = builder.addFormDataPart(key, file.getName(),
					            RequestBody.create(file, okhttp3.MediaType.parse(MediaType.APPLICATION_OCTET_STREAM.getType())));
					} else {
						builder = builder.addFormDataPart(key, value.toString());
					}
				}
			}
			requestBody = builder.build();
		}
		final String loggingRequestBody = body instanceof File
				? "<file data>"
				: requestBody2String(body, headers);

		// log debug message
		if (log.isDebugEnabled()) {
			final StringBuilder debugMessage = new StringBuilder();
			debugMessage.append("Sending "); debugMessage.append(method); debugMessage.append(" request to "); debugMessage.append(loggingRequestUrl);
			debugMessage.append("\nrequest headers:");
			appendHeaders(headers, debugMessage);
			debugMessage.append("\nrequest body:\n"); debugMessage.append(loggingRequestBody);
			log.debug(debugMessage.toString());
		}

		// create the request
		Request.Builder builder = new Request.Builder()
				.url(httpUrl)
				.method(method.toString(), requestBody);
		for (final String key: headers.keySet()) {
			// we join multiple values in a CSV list ourselves
			// if we execute addHeader more than once, multiple headers are added instead of appending the value to the existing header
			// (and we do not want that)
			final String value = String.join(",", headers.getValuesAsList(key));
			builder = builder.addHeader(key, value);
		}
		final Request okHttpRequest = builder.build();

		// set timeout values
		okhttp3.OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
			    .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
			    .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
			    .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
			    .callTimeout(callTimeout, TimeUnit.MILLISECONDS);

		// use proxy if required
		if (proxy != null)
			clientBuilder = clientBuilder.proxy(proxy);

		// build the client
		okHttpClient = clientBuilder.build();

		// execute the request
		HttpStatus loggingHttpStatus = null;
		String loggingResponseBody = null;
		try {
			final okhttp3.Response okHttpResponse = okHttpClient.newCall(okHttpRequest).execute();

			// parse the response
			final ResponseBody responseBody = okHttpResponse.body();
			// TODO: this will fail if the returned HTTP status is not part of Spring's HttpStatus enum
			final HttpStatus httpStatus = HttpStatus.valueOf(okHttpResponse.code());

			if (!okHttpResponse.isSuccessful()) {
				loggingHttpStatus = httpStatus;
				loggingResponseBody = StringTools.toString(responseBody.byteStream());
				throw new IOException("HTTP response has a non 2xx HTTP status.");
			}

			final Response<O> toReturn = new Response<>();
			toReturn.setRequestUrl(httpUrl.toString());
			if (bodyParser.getTargetFile() != null) {
				// request is a file download request => stream response body to target file
				loggingResponseBody = "<file data>";
				final FileOutputStream fos = new FileOutputStream(bodyParser.getTargetFile());
				StreamTools.copy(responseBody.byteStream(), fos);
				fos.close();
			} else {
				// request has a JSON or null response
				final String bodyData = StringTools.toString(responseBody.byteStream());
				if (bodyData != null && bodyData.trim().length() > 0)
					bodyParser.parseBody(bodyData, toReturn);
				loggingResponseBody = bodyData;
			}
			responseBody.close();
			final HttpHeaders responseHeaders = new HttpHeaders();
			final Headers okHttpHeaders = okHttpResponse.headers();
			for (final String name : okHttpHeaders.names()) {
				for (final String value : okHttpHeaders.values(name)) {
					responseHeaders.add(name, value);
				}
			}

			// log debug message
			if (log.isDebugEnabled()) {
				final StringBuilder debugMessage = new StringBuilder();
				debugMessage.append("Got response from "); debugMessage.append(method); debugMessage.append(" request to "); debugMessage.append(loggingRequestUrl);
				debugMessage.append("\nresponse headers:");
				appendHeaders(responseHeaders, debugMessage);
				debugMessage.append("\nresponse body:\n"); debugMessage.append(loggingResponseBody);
				log.debug(debugMessage.toString());
			}

			toReturn.setHeaders(responseHeaders);
			toReturn.setHttpStatus(httpStatus);
			return toReturn;
		} catch (final IOException e) {
			if (loggingHttpStatus == null) {
				errorResponseHandler.handleErrorResponse(e, loggingRequestUrl, loggingRequestBody);
			} else {
				errorResponseHandler.handleErrorResponse(loggingHttpStatus, loggingResponseBody, loggingRequestUrl, loggingRequestBody);
			}
			throw new ErrorResponseException();
		}
	}

	private void appendHeaders (final HttpHeaders headers, final StringBuilder sb) {
		for (final String name : headers.keySet()) {
			sb.append("\n "); sb.append(name); sb.append(": ");
			boolean moreThanOne = false;
			for (final String value : headers.get(name)) {
				if (moreThanOne) sb.append(", ");
				sb.append(value);
				moreThanOne = true;
			}
		}
	}

}

enum ErrorResponseType {

	STATUS_CODE, STATUS_VALUE, EXCEPTION;

}

class ErrorResponseCache implements ErrorResponseHandler {

	private ErrorResponseType type = null;
	private HttpStatus statusCode;
	private int statusCodeValue;
	private Exception e;
	private String responseBody;
	@Getter private String requestUrl;
	private String requestBody;
	private final ErrorResponseHandler targetHandler;

	public ErrorResponseCache(final ErrorResponseHandler targetHandler) {
		this.targetHandler = targetHandler;
	}

	@Override
	public void handleErrorResponse(final HttpStatus statusCode, final String responseBody, final String requestUrl, final String requestBody) {
		type = ErrorResponseType.STATUS_CODE;
		this.statusCode = statusCode;
		this.responseBody = responseBody;
		this.requestUrl = requestUrl;
		this.requestBody = requestBody;
	}

	@Override
	public void handleErrorResponse(final int statusCode, final String responseBody, final String requestUrl, final String requestBody) {
		type = ErrorResponseType.STATUS_VALUE;
		this.statusCodeValue = statusCode;
		this.responseBody = responseBody;
		this.requestUrl = requestUrl;
		this.requestBody = requestBody;
	}

	@Override
	public void handleErrorResponse(final Exception e, final String requestUrl, final String requestBody) {
		type = ErrorResponseType.EXCEPTION;
		this.e = e;
		this.requestUrl = requestUrl;
		this.requestBody = requestBody;
	}

	public boolean checkRetryFilter (final ErrorResponseRetryFilter retryFilter) {
		switch (type) {
		case STATUS_CODE:
			return retryFilter.retry(statusCode, responseBody, requestUrl, requestBody);
		case STATUS_VALUE:
			return retryFilter.retry(statusCodeValue, responseBody, requestUrl, requestBody);
		case EXCEPTION:
			return retryFilter.retry(e, requestUrl, requestBody);
		default:
			throw new RuntimeException("Unsupported ErrorResponseType: " + type);
		}
	}

	public Integer getStatusCodeValue () {
		if (type == ErrorResponseType.STATUS_CODE)
			return statusCode.value();
		else if (type == ErrorResponseType.STATUS_VALUE)
			return statusCodeValue;
		else
			return null;
	}

	public String getStatusCodePhrase () {
		if (type == ErrorResponseType.STATUS_CODE)
			return statusCode.getReasonPhrase();
		else if (type == ErrorResponseType.EXCEPTION)
			return e.getMessage();
		else
			return null;
	}

	/**
	 * Flushes the cached error response (if any) to the target ErrorResponseHandler
	 */
	public void flush() {
		if (type != null) {
			switch (type) {
			case STATUS_CODE:
				targetHandler.handleErrorResponse(statusCode, responseBody, requestUrl, requestBody);
				break;
			case STATUS_VALUE:
				targetHandler.handleErrorResponse(statusCodeValue, responseBody, requestUrl, requestBody);
				break;
			case EXCEPTION:
				targetHandler.handleErrorResponse(e, requestUrl, requestBody);
				break;
			}
		}
	}

}
