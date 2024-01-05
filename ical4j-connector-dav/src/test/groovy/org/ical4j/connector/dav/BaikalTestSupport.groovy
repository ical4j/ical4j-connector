package org.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider

interface BaikalTestSupport {

    default String getContainerImageName() { 'ckulka/baikal:nginx' }

    default int getContainerPort() { 80 }

    default String getConfigPath() { 'src/test/resources/baikal/baikal.yaml' }

    default String getContainerConfigPath() { '/var/www/baikal/config/baikal.yaml.1' }

    default String getRepositoryPath() { '/dav.php' }

    default PathResolver getPathResolver() { PathResolver.Defaults.BAIKAL }

    default CredentialsProvider getCredentialsProvider() {
        Credentials credentials = new UsernamePasswordCredentials('admin', 'admin');
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        credentialsProvider
    }
}
