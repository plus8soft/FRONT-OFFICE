/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import web.configuration.Settings;

@Configuration
public class FineractConfiguration {

    @Autowired
    private Settings settings;

    @Bean
    public RestTemplate fineractRestTemplate() throws NoSuchAlgorithmException, KeyManagementException {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (settings.isFineractSslTrustAll()) {
            SSLSocketFactory socketFactory = createTrustAllSocketFactory();
            HostnameVerifier hostnameVerifier = (hostname, session) -> true;
            requestFactory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                    if (connection instanceof HttpsURLConnection) {
                        HttpsURLConnection https = (HttpsURLConnection) connection;
                        https.setSSLSocketFactory(socketFactory);
                        https.setHostnameVerifier(hostnameVerifier);
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };
        }
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(30_000);
        return new RestTemplate(requestFactory);
    }

    private static SSLSocketFactory createTrustAllSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new java.security.SecureRandom());
        return sslContext.getSocketFactory();
    }
}
