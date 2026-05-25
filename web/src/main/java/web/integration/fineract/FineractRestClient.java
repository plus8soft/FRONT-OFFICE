/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.configuration.Settings;

@Component
public class FineractRestClient {

    private static final Logger LOG = LogManager.getLogger(FineractRestClient.class);

    private static final String HEADER_TENANT = "Fineract-Platform-TenantId";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String MEDIA_JSON = "application/json";

    @Autowired
    private Settings settings;

    public boolean isConfigured() {
        return settings.isFineractEnabled()
                && settings.getFineractBaseUrl() != null
                && !settings.getFineractBaseUrl().trim().isEmpty()
                && settings.getFineractUsername() != null
                && !settings.getFineractUsername().isEmpty();
    }

    public JsonObject get(String path) {
        return exchange("GET", path, null);
    }

    public JsonObject post(String path, JsonObject body) {
        return exchange("POST", path, body);
    }

    public JsonObject put(String path, JsonObject body) {
        return exchange("PUT", path, body);
    }

    private JsonObject exchange(String method, String path, JsonObject body) {
        HttpURLConnection connection = null;
        String requestPath = normalizePath(path);
        try {
            connection = openConnection(buildUrl(requestPath));
            connection.setRequestMethod(method);
            applyHeaders(connection);
            if (body != null) {
                connection.setDoOutput(true);
                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setRequestProperty(HEADER_CONTENT_TYPE, MEDIA_JSON);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(payload);
                }
            }
            int status = connection.getResponseCode();
            String responseBody = readBody(status >= 400 ? connection.getErrorStream() : connection.getInputStream());
            if (status < 200 || status >= 300) {
                throw new FineractException("Fineract " + method + " " + requestPath + " failed with HTTP " + status + ": " + responseBody);
            }
            if (responseBody == null || responseBody.trim().isEmpty()) {
                return new JsonObject();
            }
            JsonElement parsed = new JsonParser().parse(responseBody);
            if (parsed.isJsonObject()) {
                return parsed.getAsJsonObject();
            }
            JsonObject wrapper = new JsonObject();
            wrapper.add("value", parsed);
            return wrapper;
        } catch (FineractException e) {
            throw e;
        } catch (Exception e) {
            throw new FineractException("Fineract request failed: " + method + " " + requestPath, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String normalizePath(String path) {
        return path == null ? "" : path.trim();
    }

    private String buildUrl(String path) {
        String base = settings.getFineractBaseUrl() == null ? "" : settings.getFineractBaseUrl().trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!path.isEmpty() && !path.startsWith("/")) {
            path = "/" + path;
        }
        return base + path;
    }

    private HttpURLConnection openConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        if (settings.isFineractSslTrustAll() && "https".equalsIgnoreCase(url.getProtocol())) {
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new java.security.SecureRandom());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier((hostname, session) -> true);
            return connection;
        }
        return (HttpURLConnection) url.openConnection();
    }

    private void applyHeaders(HttpURLConnection connection) {
        String credentials = settings.getFineractUsername() + ":" + settings.getFineractPassword();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encoded);
        connection.setRequestProperty(HEADER_TENANT, settings.getFineractTenantId());
        connection.setRequestProperty(HEADER_ACCEPT, MEDIA_JSON);
        connection.setConnectTimeout(settings.getFineractConnectTimeoutMs());
        connection.setReadTimeout(settings.getFineractReadTimeoutMs());
    }

    private static String readBody(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
