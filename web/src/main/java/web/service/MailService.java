/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service;

import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import web.configuration.JksStore;
import web.configuration.Settings;

@Service
@Log4j2
public class MailService {

    private static final String SIGN_ALIAS = "mail-sign";

    @Autowired
    private Settings settings;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JksStore jksStore;

    private SMIMESignedGenerator signGenerator;

    @PostConstruct
    private void init() {
        try {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            // Try to get key and certificate, but don't fail if keystore is empty
            PrivateKey privateKey = null;
            X509Certificate certificate = null;
            try {
                privateKey = jksStore.getPrivateKey(SIGN_ALIAS, settings.getMailSignPassword());
                certificate = jksStore.getCertificate(SIGN_ALIAS);
            } catch (Exception e) {
                log.warn("Cannot get key/certificate from keystore (keystore may be empty): {}", e.getMessage());
            }
            // Only initialize signer if key and certificate are available (keystore is not empty)
            if (privateKey != null && certificate != null) {
                signGenerator = new SMIMESignedGenerator();
                signGenerator.addSignerInfoGenerator(
                        new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").build("SHA1withRSA", privateKey, certificate));
                signGenerator.addCertificates(new JcaCertStore(Collections.singletonList(certificate)));
            } else {
                // Keystore is empty, mail signing will not be available
                log.warn("Mail signing is disabled: keystore is empty or key '{}' not found", SIGN_ALIAS);
            }
        } catch (Exception e) {
            log.warn("Failed to initialize mail signing, mail will be sent without signature: {}", e.getMessage());
        }
    }

    public void sendMail(MailServiceConsumer<MimeMessageHelper> consumer) {
        try {
            JavaMailSender mailSender = applicationContext.getBean(JavaMailSender.class);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(settings.getMailFrom());
            consumer.accept(helper);
            // Sign message only if signGenerator is initialized
            if (signGenerator != null) {
                mimeMessage.setContent(signGenerator.generate(mimeMessage));
            }
            mailSender.send(mimeMessage);
        } catch (MessagingException | SMIMEException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    public interface MailServiceConsumer<T> {

        void accept(T param) throws MessagingException;
    }
}
