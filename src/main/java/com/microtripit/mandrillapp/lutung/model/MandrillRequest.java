/**
 * 
 */
package com.microtripit.mandrillapp.lutung.model;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;

/**
 * @author rschreijer
 * @since Mar 16, 2013
 */
public final class MandrillRequest<OUT> implements RequestModel<OUT> {
	private static final Logger log = Logger.getLogger(MandrillRequest.class.getName());

	private final String url;
	private final Class<OUT> responseContentType;
	private final Map<String, ? extends Object> requestParams;

	public MandrillRequest(final String url, final Map<String, ? extends Object> params, final Class<OUT> responseType) {

		if (responseType == null) {
			throw new NullPointerException();

		}
		this.url = url;
		this.requestParams = params;
		this.responseContentType = responseType;
	}

	public final String getUrl() {
		return url;
	}

	public final HTTPRequest getRequest() throws IOException {

		FetchOptions options = FetchOptions.Builder.withDefaults();

		HTTPRequest request = new HTTPRequest(new URL(url), HTTPMethod.POST, options);
		final String paramsStr = LutungGsonUtils.getGson().toJson(requestParams, requestParams.getClass());
		log.info("raw content for request:\n" + paramsStr);

		request.setPayload(paramsStr.getBytes("UTF-8"));
		request.setHeader(new HTTPHeader("content-type", "application/json"));

		return request;

	}

	public final boolean validateResponseStatus(final int httpResponseStatus) {
		return (httpResponseStatus == 200);
	}

	public final OUT handleResponse(String raw) throws HandleResponseException {
		try {
			log.info("raw content from response:\n" + raw);
			return LutungGsonUtils.getGson().fromJson(raw, responseContentType);

		} catch (final Throwable t) {
			String msg = "Error handling Mandrill response " + ((raw != null) ? ": '" + raw + "'" : "");
			throw new HandleResponseException(msg, t);

		}
	}

}
