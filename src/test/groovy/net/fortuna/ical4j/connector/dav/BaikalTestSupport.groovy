package net.fortuna.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider

interface BaikalTestSupport {

    default String getContainerImageName() { 'ckulka/baikal:nginx' }

    default int getContainerPort() { 80 }

    default PathResolver getPathResolver() { PathResolver.Defaults.BAIKAL }

    default CredentialsProvider getCredentialsProvider() {
        Credentials credentials = new UsernamePasswordCredentials('admin', 'admin');
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        credentialsProvider
    }
}
