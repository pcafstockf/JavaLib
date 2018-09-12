package com.bytelightning.oss.lib.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public class StringResponseHandler implements ResponseHandler<String> {
	@Override
	public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		StatusLine status = response.getStatusLine();
		int statusCode = status.getStatusCode();
		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity rsp = response.getEntity();
			return rsp != null ? EntityUtils.toString(rsp) : null;
		} else
			throw new HttpResponseException(statusCode, status.getReasonPhrase());
	}
}