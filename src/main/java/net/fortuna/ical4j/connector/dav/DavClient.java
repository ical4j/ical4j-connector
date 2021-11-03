/**
 * Copyright (c) 2012, Ben Fortuna
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

import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.dav.enums.SupportedFeature;
import net.fortuna.ical4j.connector.dav.property.CSDavPropertyName;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DavClient {

	/**
	 * The underlying HTTP client.
	 */
	protected HttpClient httpClient;

	protected HttpClientContext httpClientContext;

	private String principalPath;

	private String userPath;

	private String bearerAuth;

	private CredentialsProvider credentialsProvider;

	private final boolean preemptiveAuth;

	/**
	 * The HTTP client configuration.
	 */
	protected HttpHost hostConfiguration;

	public DavClient(URL url, String principalPath, String userPath) {
		this(url, principalPath, userPath, false);
	}

	public DavClient(URL url, String principalPath, String userPath, boolean preemptiveAuth) {
		this.principalPath = principalPath;
		this.userPath = userPath;
		this.preemptiveAuth = preemptiveAuth;

		hostConfiguration = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
	}

	void begin() {
		httpClient = HttpClients.createDefault();
	}

	void begin(CredentialsProvider credentialsProvider) {
		httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

		httpClientContext = HttpClientContext.create();

		if (preemptiveAuth) {
			AuthCache authCache = new BasicAuthCache();
			authCache.put(hostConfiguration, new BasicScheme());
			httpClientContext.setAuthCache(authCache);
		}
	}

	ArrayList<SupportedFeature> begin(String bearerAuth) throws IOException, FailedOperationException {
		this.bearerAuth = bearerAuth;
		return getSupportedFeatures();
	}

	ArrayList<SupportedFeature> begin(String username, char[] password) throws IOException, FailedOperationException {
		Credentials credentials = new UsernamePasswordCredentials(username, new String(password));

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(hostConfiguration.getHostName(), hostConfiguration.getPort()),
				credentials);

		this.credentialsProvider = credentialsProvider;
		return getSupportedFeatures();
	}

	ArrayList<SupportedFeature> getSupportedFeatures() throws IOException, FailedOperationException {
		begin(credentialsProvider);

		DavPropertyNameSet props = new DavPropertyNameSet();
		props.add(DavPropertyName.RESOURCETYPE);
		props.add(CSDavPropertyName.CTAG);
		DavPropertyName owner = DavPropertyName.create("owner", DavConstants.NAMESPACE);
		props.add(owner);

		HttpPropfind aGet = new HttpPropfind(principalPath, DavConstants.PROPFIND_BY_PROPERTY, props, 0);
		if (bearerAuth != null) {
			aGet.addHeader("Authorization", "Bearer " + bearerAuth);
		}

		RequestConfig.Builder builder = aGet.getConfig() == null ? RequestConfig.custom() : RequestConfig.copy(aGet.getConfig());
		builder.setAuthenticationEnabled(true);
		if (credentialsProvider != null) {
			// Added to support iCal Server, who don't support Basic auth at all,
			// only Kerberos and Digest
			List<String> authPrefs = new ArrayList<String>(2);
			authPrefs.add(AuthSchemes.DIGEST);
			authPrefs.add(AuthSchemes.BASIC);
			builder.setTargetPreferredAuthSchemes(authPrefs);
		}
		RequestConfig config = builder.build();
		aGet.setConfig(config);

		ArrayList<SupportedFeature> supportedFeatures = new ArrayList<SupportedFeature>();

		HttpResponse response = httpClient.execute(hostConfiguration, aGet, httpClientContext);

		if (response.getStatusLine().getStatusCode() >= 300) {
			throw new FailedOperationException(String.format("Principals not found at [%s]", userPath));
		} else {
			Header[] davHeaders = response.getHeaders(net.fortuna.ical4j.connector.dav.DavConstants.HEADER_DAV);
			for (int headerIndex = 0; headerIndex < davHeaders.length; headerIndex++) {
				Header header = davHeaders[headerIndex];
				HeaderElement[] elements = header.getElements();
				for (int elementIndex = 0; elementIndex < elements.length; elementIndex++) {
					String feature = elements[elementIndex].getName();
					if (feature != null) {
						SupportedFeature supportedFeature = SupportedFeature.findByDescription(feature);
						if (supportedFeature != null) {
							supportedFeatures.add(supportedFeature);
						}
					}
				}
			}
		}
		return supportedFeatures;
	}

	public HttpResponse execute(HttpRequestBase method) throws IOException {
		return execute(hostConfiguration, method);
	}

	public HttpResponse execute(HttpHost _hostConfiguration, HttpRequestBase method) throws IOException {
		return httpClient.execute(_hostConfiguration, method, httpClientContext);
	}
}
