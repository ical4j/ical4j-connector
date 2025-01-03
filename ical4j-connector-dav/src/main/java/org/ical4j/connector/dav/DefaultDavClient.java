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
package org.ical4j.connector.dav;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.vcard.VCard;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestDefaultHeaders;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.*;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.dav.method.*;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.ical4j.connector.dav.property.PropertyNameSets;
import org.ical4j.connector.dav.request.CalendarQuery;
import org.ical4j.connector.dav.request.MkCalendarEntity;
import org.ical4j.connector.dav.request.MkColEntity;
import org.ical4j.connector.dav.response.*;
import org.jetbrains.annotations.NotNull;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultDavClient implements CalDavSupport, CardDavSupport {

	/**
	 * The HTTP client configuration.
	 */
	private final HttpHost hostConfiguration;

	private final String repositoryPath;

	private final DavClientConfiguration clientConfiguration;

	/**
	 * The underlying HTTP client.
	 */
	protected HttpClient httpClient;

	protected HttpClientContext httpClientContext;

	/**
	 * Create a disconnected DAV client instance.
	 * @param href a URL string representing a DAV host location
	 * @param clientConfiguration configuration parameters specific to a DAV client
	 */
	DefaultDavClient(@NotNull String href, DavClientConfiguration clientConfiguration) throws MalformedURLException {
		this(URI.create(href).toURL(), clientConfiguration);
	}

	/**
	 * Create a disconnected DAV client instance.
	 * @param href a URL string representing a DAV host location
	 * @param clientConfiguration configuration parameters specific to a DAV client
	 */
	DefaultDavClient(@NotNull URL href, DavClientConfiguration clientConfiguration) {

		this.hostConfiguration = new HttpHost(href.getHost(), href.getPort(), href.getProtocol());
		this.repositoryPath = href.getPath().isEmpty() ? "/" : href.getPath();
		this.clientConfiguration = clientConfiguration;
	}

	void begin() {
		begin((CredentialsProvider) null);
	}

	public List<SupportedFeature> begin(String bearerAuth) throws IOException, FailedOperationException {
		clientConfiguration.withDefaultHeader("Authorization", "Bearer " + bearerAuth);
		begin();
		return getSupportedFeatures();
	}

	public List<SupportedFeature> begin(String username, char[] password) throws IOException, FailedOperationException {
		var credentials = new UsernamePasswordCredentials(username, new String(password));

		var credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(hostConfiguration), credentials);
		begin(credentialsProvider);

		return getSupportedFeatures();
	}

	void begin(CredentialsProvider credentialsProvider) {

		var builder = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider);

		Collection<Header> defaultHeaders = clientConfiguration.getDefaultHeaders().entrySet().stream()
				.map(e -> new BasicHeader(e.getKey(), e.getValue())).collect(Collectors.toList());
		builder.addInterceptorLast(new RequestDefaultHeaders(defaultHeaders));

		if (clientConfiguration.isFollowRedirects()) {
			builder.setRedirectStrategy(new LaxRedirectStrategy());
		}
		httpClient = builder.build();
		httpClientContext = HttpClientContext.create();

		if (clientConfiguration.isPreemptiveAuth()) {
			AuthCache authCache = new BasicAuthCache();
			authCache.put(hostConfiguration, new BasicScheme());
			httpClientContext.setAuthCache(authCache);
		}
	}

	public HttpHost getHostConfiguration() {
		return hostConfiguration;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public DavClientConfiguration getClientConfiguration() {
		return clientConfiguration;
	}

	public List<SupportedFeature> getSupportedFeatures() throws IOException {
		var aGet = new HttpPropfind(repositoryPath, DavConstants.PROPFIND_BY_PROPERTY,
				PropertyNameSets.PROPFIND_SUPPORTED_FEATURES, 0);

		var builder = aGet.getConfig() == null ? RequestConfig.custom() : RequestConfig.copy(aGet.getConfig());
		builder.setAuthenticationEnabled(true);
		// Added to support iCal Server, who don't support Basic auth at all,
		// only Kerberos and Digest
		List<String> authPrefs = new ArrayList<>(2);
		authPrefs.add(AuthSchemes.DIGEST);
		authPrefs.add(AuthSchemes.BASIC);
		builder.setTargetPreferredAuthSchemes(authPrefs);

		var config = builder.build();
		aGet.setConfig(config);
		return execute(aGet, new GetSupportedFeatures());
	}

	@Override
	public void mkCalendar(String path, DavPropertySet properties) throws IOException, ObjectStoreException, DavException {
		var mkCalendarMethod = new MkCalendar(resolvePath(path));
		mkCalendarMethod.setEntity(XmlEntity.create(new MkCalendarEntity().withProperties(properties)));
		var httpResponse = execute(mkCalendarMethod);
		if (!mkCalendarMethod.succeeded(httpResponse)) {
			throw new ObjectStoreException(httpResponse.getStatusLine().getStatusCode() + ": "
					+ httpResponse.getStatusLine().getReasonPhrase());
		}
	}

	@Override
	public void mkCol(String path, DavPropertySet properties) throws ObjectStoreException, DavException, IOException {
		var mkcolMethod = new HttpMkcol(resolvePath(path));
		mkcolMethod.setEntity(XmlEntity.create(new MkColEntity().withProperties(properties)));
		var httpResponse = execute(mkcolMethod);
		if (!mkcolMethod.succeeded(httpResponse)) {
			throw new ObjectStoreException(httpResponse.getStatusLine().getStatusCode() + ": "
					+ httpResponse.getStatusLine().getReasonPhrase());
		}
	}

	@Override
	public List<ResourceProps> propFind(String path, DavPropertyNameSet propertyNames) throws IOException {
		return propFind(path, propertyNames, new GetResourceProperties());
	}

	@Override
	public <T> T propFind(String path, DavPropertyNameSet propertyNames, ResponseHandler<T> handler)
			throws IOException {
		var aGet = new HttpPropfind(resolvePath(path), propertyNames, 0);
		return execute(aGet, handler);
	}

	/**
	 *
	 * @param path
	 * @param propertyNames
	 * @param resourceTypes
	 * @return
	 * @throws IOException
	 * @deprecated use {@link WebDavSupport#propFind(String, DavPropertyNameSet, ResponseHandler)} instead
	 */
	@Deprecated
	public Map<String, DavPropertySet> propFindResources(String path, DavPropertyNameSet propertyNames,
														 ResourceType...resourceTypes) throws IOException {
		return propFind(path, propertyNames, new GetCollections(resourceTypes));
	}

	@Override
	public List<ResourceProps> propFindType(String path, int type) throws IOException {
		var aGet = new HttpPropfind(resolvePath(path), type, 1);

//		RequestConfig config = RequestConfig.custom().setAuthenticationEnabled(true).build();
//		aGet.setConfig(config);
		return execute(aGet, new GetResourceProperties());
	}

	@Override
	public List<ResourceProps> report(String path, CalendarQuery query, DavPropertyNameSet propertyNames)
			throws IOException, ParserConfigurationException {

		var info = new ReportInfo(CalDavPropertyName.CALENDAR_QUERY, 1, propertyNames);
		info.setContentElement(query.build());
		
		return report(path, info, new GetResourceProperties());
	}

	@Override
	public <T> T report(String path, ReportInfo info, ResponseHandler<T> handler) throws IOException,
			ParserConfigurationException {

		var method = new HttpReport(resolvePath(path), info);
		return execute(method, handler);
	}

	@Override
	public <T> T get(String path, ResponseHandler<T> handler) throws IOException {
		var httpGet = new HttpGet(resolvePath(path));
		return execute(httpGet, handler);
	}

	@Override
	public <T> T head(String path, ResponseHandler<T> handler) throws IOException {
		var httpHead = new HttpHead(resolvePath(path));
		return execute(httpHead, handler);
	}

	@Override
	public void put(String path, Calendar calendar, String etag) throws IOException, FailedOperationException {
		var httpPut = new PutCalendar(resolvePath(path));
		if (etag != null) {
			httpPut.setEtag(etag);
		}
		httpPut.setCalendar(calendar);
		var httpResponse = execute(httpPut);
		if (!httpPut.succeeded(httpResponse)) {
			var w = new StringWriter();
			httpResponse.getEntity().writeTo(WriterOutputStream.builder().setWriter(w).get());
			throw new FailedOperationException(
					"Error creating calendar on server: " + httpResponse.getStatusLine() + "-" + w);
		}
	}

	@Override
	public void put(String path, VCard card, String etag) throws IOException, FailedOperationException {
		var httpPut = new PutVCard(resolvePath(path));
		httpPut.setEtag(etag);
		httpPut.setVCard(card);
		var httpResponse = execute(httpPut);
		if (!httpPut.succeeded(httpResponse)) {
			throw new FailedOperationException(
					"Error creating card on server: " + httpResponse.getStatusLine());
		}
	}

	@Override
	public void copy(String src, String dest) throws IOException {
		var method = new HttpCopy(resolvePath(src), resolvePath(dest), true, false);
		execute(method, response -> {
			try {
				method.checkSuccess(response);
			} catch (DavException e) {
				throw new RuntimeException(e);
			}
			return true;
		});
	}

	@Override
	public void move(String src, String dest) throws IOException {
		var method = new HttpMove(resolvePath(src), resolvePath(dest), true);
		execute(method, response -> {
			try {
				method.checkSuccess(response);
			} catch (DavException e) {
				throw new RuntimeException(e);
			}
			return true;
		});
	}

	@Override
	public void delete(String path) throws IOException, DavException {
		var method = new HttpDelete(resolvePath(path));
		execute(method, response -> {
			try {
				method.checkSuccess(response);
			} catch (DavException e) {
				throw new RuntimeException(e);
			}
			return true;
		});
	}

	public List<ScheduleResponse> freeBusy(String path, Calendar query, Organizer organizer) throws IOException {
		var freeBusy = new FreeBusy(resolvePath(path), organizer);
		freeBusy.setQuery(query);
		return execute(freeBusy, new GetFreeBusyData());
	}

	@Override
	public MultiStatusResponse propPatch(String path, List<? extends PropEntry> changeList) throws IOException {
		var method = new HttpProppatch(resolvePath(path), changeList);
		return execute(method, response -> {
			try {
				method.checkSuccess(response);
				return method.getResponseBodyAsMultiStatus(response).getResponses()[0];
			} catch (DavException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void post() {

	}

	@Override
	public void lock(String path) {

	}

	@Override
	public void unlock(String path) {

	}

	public List<Attendee> findPrincipals(String path, PrincipalPropertySearchInfo info) throws IOException {
		var principalPropertySearch = new PrincipalPropertySearch(resolvePath(path), info);
		return execute(principalPropertySearch, new GetPrincipals());
	}

	private String resolvePath(String path) {
		if (path == null) {
			return repositoryPath;
		} else if (path.startsWith("/")) {
			return (repositoryPath + path).replaceAll("/+", "/");
		} else {
			return (repositoryPath + "/" + path).replaceAll("/+", "/");
		}
	}

	private HttpResponse execute(BaseDavRequest method) throws IOException, DavException {
		var response = httpClient.execute(hostConfiguration, method, httpClientContext);
		method.checkSuccess(response);
		return response;
	}

	private HttpResponse execute(HttpRequest method) throws IOException {
		return httpClient.execute(hostConfiguration, method, httpClientContext);
	}

	private <T> T execute(HttpRequest method, ResponseHandler<T> handler) throws IOException {
		return httpClient.execute(hostConfiguration, method, handler, httpClientContext);
	}
}
