/**
 * 
 */
package com.microtripit.mandrillapp.lutung.model;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError.MandrillError;

/**
 * @author rschreijer
 * @since Feb 21, 2013
 */
public final class MandrillRequestDispatcher {

	private final static Logger log = Logger.getLogger(MandrillRequestDispatcher.class.getName());

	public static final <T> T execute(final RequestModel<T> requestModel) throws MandrillApiError, IOException {

		HTTPResponse response = null;
		log.fine("Starting URLFetch service");
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		HTTPRequest request = requestModel.getRequest();
		// client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
		// client.getParams().getParameter(CoreProtocolPNames.USER_AGENT) +
		// "/Lutung-0.1");
		// // use proxy?
		// HttpConnectionParams.setSoTimeout(client.getParams(),
		// SOCKET_TIMEOUT_MILLIS);
		// HttpConnectionParams.setConnectionTimeout(client.getParams(),
		// CONNECTION_TIMEOUT_MILLIS);

		log.info("sending request '" + requestModel.getUrl() + "'");

		response = urlFetchService.fetch(request);

		int status = response.getResponseCode();
		byte[] content = response.getContent();
		String responseText = new String(content, "UTF-8");

		if (requestModel.validateResponseStatus(status)) {
			try {
				return requestModel.handleResponse(responseText);

			} catch (final HandleResponseException e) {
				throw new IOException("Failed to parse response from request '" + requestModel.getUrl() + "'", e);

			}

		} else {
			// ==> compile mandrill error!
			final MandrillError error = LutungGsonUtils.getGson().fromJson(responseText, MandrillError.class);
			throw new MandrillApiError("Unexpected http status in response: " + status + " (" + responseText + ")")
					.withError(error);

		}

	}

}
