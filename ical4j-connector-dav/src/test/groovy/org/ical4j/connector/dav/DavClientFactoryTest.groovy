package org.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import spock.lang.Specification

class DavClientFactoryTest extends Specification {

    def 'test creation of dav client'() {
        given: 'a client factory'
        def clientFactory = new DavClientFactory().withPreemptiveAuth(true)
                .withFollowRedirects(true)

        when: 'a new instance is created'
        DefaultDavClient client = clientFactory.newInstance('https://caldav.example.com')

        then: 'it is configured as expected'
        client.hostConfiguration.hostName == 'caldav.example.com'
        client.hostConfiguration.schemeName == 'https'
        client.hostConfiguration.port == -1
        client.repositoryPath == '/'
        client.clientConfiguration.followRedirects
        client.clientConfiguration.preemptiveAuth
    }

    def 'test creation of dav client with authentication'() {
        given: 'a client factory'
        def clientFactory = new DavClientFactory().withPreemptiveAuth(true)
                .withFollowRedirects(true)

        and: 'a credentials provider'
        Credentials credentials = new UsernamePasswordCredentials('alice', 'secret');
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope('caldav.example.com', 443), credentials);
        clientFactory = clientFactory.withCredentialsProvider(credentialsProvider)

        when: 'a new instance is created'
        DefaultDavClient client = clientFactory.newInstance('https://caldav.example.com')

        then: 'it is configured as expected'
        client.hostConfiguration.hostName == 'caldav.example.com'
        client.hostConfiguration.schemeName == 'https'
        client.hostConfiguration.port == -1
        client.repositoryPath == '/'
        client.clientConfiguration.followRedirects
        client.clientConfiguration.preemptiveAuth
    }
}
