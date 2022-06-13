package be.vito.rma.resttools.client.tools;

import org.springframework.web.util.UriUtils;

import be.vito.rma.resttools.client.dtos.Endpoint;
import be.vito.rma.resttools.client.dtos.RequestParameter;
import okhttp3.HttpUrl;

/**
 * @author (c) 2019 Stijn.VanLooy@vito.be
 *
 */
public class UrlTools {

	public static HttpUrl createUrl (final String serviceUrl, final Endpoint endpoint, final RequestParameter... parameters) {
		return createUrl (HttpUrl.parse(serviceUrl), endpoint, parameters);
	}

	public static HttpUrl createUrl (final HttpUrl baseUrl, final Endpoint endpoint, final RequestParameter... parameters) {
		/*
		 * We still use Spring's UriUtils to encode the path elements.
		 * If we don't, there are problems with these chars in the URL path: ; / . [ ] \ :
		 * There are still problems with some chars.
		 * Also see: be.vito.rma.resttools.api.consts.UrlEncoding in resttools-api
		 */
		HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
			.scheme(baseUrl.scheme()).username(baseUrl.username()).password(baseUrl.password())
			.host(baseUrl.host()).port(baseUrl.port()).fragment(baseUrl.fragment());
		for (final String pathSegment : baseUrl.pathSegments())
			urlBuilder = urlBuilder.addEncodedPathSegment(UriUtils.encodePath(pathSegment, "UTF-8"));
//			urlBuilder = urlBuilder.addPathSegment(pathSegment);
		if (endpoint.getPathElements() != null) {
			for (final Object pathElement : endpoint.getPathElements()) {
				urlBuilder = urlBuilder.addEncodedPathSegment(UriUtils.encodePath(pathElement.toString(), "UTF-8"));
//				urlBuilder = urlBuilder.addPathSegment(pathElement.toString());
			}
		}
		for (final RequestParameter parameter : parameters) {
			urlBuilder = urlBuilder.addQueryParameter(parameter.getKey(), parameter.getValue());
		}
		return urlBuilder.build();
	}
}
