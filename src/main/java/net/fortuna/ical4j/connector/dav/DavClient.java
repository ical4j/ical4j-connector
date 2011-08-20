/**
 * Copyright (c) 2011, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.connector.dav;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.connector.FailedOperationException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.DavMethodBase;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

public class DavClient {

	/**
	 * The underlying HTTP client.
	 */
	protected HttpClient httpClient;

	private String principalPath;

	private String userPath;

	/**
	 * The HTTP client configuration.
	 */
	protected HostConfiguration hostConfiguration;

	public DavClient(URL url, String principalPath, String userPath) {
		this.principalPath = principalPath;
		this.userPath = userPath;

		final Protocol protocol = Protocol.getProtocol(url.getProtocol());
		hostConfiguration = new HostConfiguration();
		hostConfiguration.setHost(url.getHost(), url.getPort(), protocol);
	}

	void begin() {
		httpClient = new HttpClient();
		httpClient.getParams().setAuthenticationPreemptive(false);
	}

	void begin(String username, char[] password) throws IOException, FailedOperationException {
		begin();
		Credentials credentials = new UsernamePasswordCredentials(username,
				new String(password));
		httpClient.getState().setCredentials(AuthScope.ANY, credentials);

		// Added to support iCal Server, who don't support Basic auth at all,
		// only Kerberos and Digest
		List<String> authPrefs = new ArrayList<String>(2);
		authPrefs.add(org.apache.commons.httpclient.auth.AuthPolicy.DIGEST);
		authPrefs.add(org.apache.commons.httpclient.auth.AuthPolicy.BASIC);
		httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

		// This is to get the Digest from the user
		PropFindMethod aGet = new PropFindMethod(principalPath, DavConstants.PROPFIND_ALL_PROP, 0);
		aGet.setDoAuthentication(true);
		int status = httpClient.executeMethod(hostConfiguration, aGet);
		if (status >= 300) {
			throw new FailedOperationException(String.format("Principals not found at [%s]", userPath));
		}
	}

	public int execute(HttpMethodBase method) throws IOException {
		return httpClient.executeMethod(hostConfiguration, method);
	}

	public int execute(HostConfiguration _hostConfiguration, DavMethodBase method) throws IOException {
	    return httpClient.executeMethod(_hostConfiguration, method);
	}

	protected String getUserName() {
		return ((UsernamePasswordCredentials) httpClient.getState()
				.getCredentials(AuthScope.ANY)).getUserName();
	}
}
