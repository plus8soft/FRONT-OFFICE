/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.configuration;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class AbstractStore {

    @Getter(AccessLevel.PROTECTED)
    private KeyStore keyStore;

    @PostConstruct
    private void init() {
        keyStore = load();
    }

    protected abstract KeyStore load();

    public PrivateKey getPrivateKey(String alias, String password) {
        try {
            return (PrivateKey) keyStore.getKey(alias, Optional.ofNullable(password).map(String::toCharArray).orElse(null));
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public X509Certificate getCertificate(String alias) {
        try {
            return (X509Certificate) keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public SSLSocketFactory getSslSocketFactory(KeyStore keyStore, KeyStore trustedStore) {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(getKeyManagerFactoryType());
            trustManagerFactory.init(trustedStore);
            SSLContext sslContext = SSLContext.getInstance(getSslContextType());
            sslContext.init(Optional.ofNullable(keyStore).map(store -> {
                try {
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(getKeyManagerFactoryType());
                    keyManagerFactory.init(keyStore, null);
                    return keyManagerFactory.getKeyManagers();
                } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }).orElse(null), trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public SSLSocketFactory getSslSocketFactory(KeyStore trustedStore) {
        return getSslSocketFactory(null, trustedStore);
    }

    protected abstract String getKeyManagerFactoryType();

    protected abstract String getKeyStoreType();

    protected abstract String getSslContextType();

    public KeyStore getSslKeyStore(String alias, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance(getKeyStoreType());
            keyStore.load(null, null);
            keyStore.setKeyEntry(UUID.randomUUID().toString(), getPrivateKey(alias, password), null, new Certificate[]{getCertificate(alias)});
            return keyStore;
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public KeyStore getSslTrustedKeyStore(String... aliases) {
        try {
            KeyStore keyStore = KeyStore.getInstance(getKeyStoreType());
            keyStore.load(null, null);
            for (String alias : aliases) {
                keyStore.setCertificateEntry(UUID.randomUUID().toString(), getCertificate(alias));
            }
            return keyStore;
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
