/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.configuration;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;
import javax.net.ssl.KeyManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JksStore extends AbstractStore {

    @Autowired
    private Settings settings;

    @Override
    protected KeyStore load() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            if (settings.getJksStore().exists()) {
                keyStore.load(settings.getJksStore().getInputStream(),
                              Optional.ofNullable(settings.getJksStorePassword()).map(String::toCharArray).orElse(null));
            } else {
                // Create empty keystore if file doesn't exist (for development/test environments)
                keyStore.load(null, null);
            }
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected String getKeyManagerFactoryType() {
        return KeyManagerFactory.getDefaultAlgorithm();
    }

    @Override
    protected String getKeyStoreType() {
        return KeyStore.getDefaultType();
    }

    @Override
    protected String getSslContextType() {
        return "TLS";
    }
}
