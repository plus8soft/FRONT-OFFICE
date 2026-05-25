/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.encryption;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import web.entity.core.Certificate;

@Log4j2
@Service
@Lazy
public class CryptoService {

    private CertificateFactory certificateFactory;

    @PostConstruct
    public void init() {
        try {
            // Add BouncyCastle provider if not already added
            if (java.security.Security.getProvider("BC") == null) {
                java.security.Security.addProvider(new BouncyCastleProvider());
            }
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize certificate factory", e);
        }
    }

    /**
     * Verifies CMS signature using standard X.509 certificates.
     * Supports RSA and ECDSA algorithms (standard for international market).
     * 
     * @param certificate Base64 encoded X.509 certificate
     * @param signedContent The content that was signed (UTF-8 string)
     * @param signature Base64 encoded CMS SignedData
     * @return true if signature is valid, false otherwise
     */
    public synchronized boolean verifySignature(String certificate, String signedContent, String signature) {
        try {
            // Decode certificate
            byte[] certBytes = Base64.getDecoder().decode(certificate);
            X509Certificate x509Cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
            
            // Decode CMS signature
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            CMSSignedData cmsSignedData = new CMSSignedData(
                    new org.bouncycastle.cms.CMSProcessableByteArray(signedContent.getBytes("UTF-8")),
                    signatureBytes);
            
            // Get signers
            Store<X509CertificateHolder> certStore = cmsSignedData.getCertificates();
            SignerInformationStore signerInfos = cmsSignedData.getSignerInfos();
            Collection<SignerInformation> signers = signerInfos.getSigners();
            
            // Verify each signer
            for (SignerInformation signer : signers) {
                Collection<X509CertificateHolder> certCollection = certStore.getMatches(signer.getSID());
                for (X509CertificateHolder certHolder : certCollection) {
                    X509Certificate signerCert = new JcaX509CertificateConverter().getCertificate(certHolder);
                    // Check if this is the certificate we're verifying
                    if (signerCert.getSerialNumber().equals(x509Cert.getSerialNumber())) {
                        // Verify signature
                        org.bouncycastle.cms.SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                                .setProvider("BC")
                                .build(signerCert);
                        if (signer.verify(verifier)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses X.509 certificate using standard Java API.
     * Supports standard X.509 certificates (RSA, ECDSA) for international market.
     * 
     * @param data Base64 encoded X.509 certificate
     * @return Certificate entity with parsed information
     */
    public synchronized Certificate parseCertificate(String data) {
        Certificate certificate = new Certificate();
        try {
            byte[] certBytes = Base64.getDecoder().decode(data);
            X509Certificate x509Cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
            
            certificate.setSerialNumber(x509Cert.getSerialNumber().toString());
            certificate.setOwner(x509Cert.getSubjectDN().getName());
            certificate.setStartDate(x509Cert.getNotBefore().toInstant());
            certificate.setEndDate(x509Cert.getNotAfter().toInstant());
            certificate.setData(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse certificate: " + e.getMessage(), e);
        }
        return certificate;
    }
}
