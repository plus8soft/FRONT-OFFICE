/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Settings {

    @Value("${datasource.url}")
    private String datasourceUrl;

    @Value("${datasource.initialpoolsize:3}")
    private Integer datasourceInitialPoolSize;

    @Value("${datasource.maxpoolsize:15}")
    private Integer datasourceMaxPoolSize;

    /**
     * Toggles calls into the core-banking adapter (web.service.back.*). When false, CRM and the rest
     * of the application run against the Front Office DB only (plus optional Fineract for clients).
     */
    @Value("${integration.back.enabled:false}")
    private boolean backEnabled;

    @Value("${scheduling:true}")
    private boolean scheduling;

    // External rate provider URL
    // Can be configured for future integrations: ECB, Fixer.io, ExchangeRate-API, etc.
    // @Value("${integration.external.rate.url:}")
    // private URL integrationExternalRateUrl;

    // Market rate provider URL
    // Can be configured for future integrations: market rate APIs, exchange aggregators, etc.
    // @Value("${integration.market.rate.url:}")
    // private String integrationMarketRateUrl;

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${mail.sign.password}")
    private String mailSignPassword;

    @Value("${smtp.host}")
    private String smtpHost;

    @Value("${smtp.port}")
    private String smtpPort;

    @Value("${smtp.username}")
    private String smtpUsername;

    @Value("${smtp.password}")
    private String smtpPassword;

    @Value("${smtp.auth}")
    private boolean smtpAuth;

    @Value("${smtp.tls}")
    private boolean smtpTls;

    @Value("${smtp.ssl}")
    private boolean smtpSsl;

    @Value("${production:false}")
    private boolean production;

    @Value("${jks.store:file:jks-store}")
    private Resource jksStore;

    @Value("${jks.store.password}")
    private String jksStorePassword;

    @Value("${lob.datasource.url}")
    private String lobDatasourceUrl;

    @Value("${lob.datasource.initialpoolsize:3}")
    private Integer lobDatasourceInitialPoolSize;

    @Value("${lob.datasource.maxpoolsize:15}")
    private Integer lobDatasourceMaxPoolSize;

    @Value("${integration.fineract.enabled:false}")
    private boolean fineractEnabled;

    @Value("${integration.fineract.base-url:}")
    private String fineractBaseUrl;

    @Value("${integration.fineract.tenant-id:default}")
    private String fineractTenantId;

    @Value("${integration.fineract.username:}")
    private String fineractUsername;

    @Value("${integration.fineract.password:}")
    private String fineractPassword;

    @Value("${integration.fineract.office-id:1}")
    private Long fineractOfficeId;

    @Value("${integration.fineract.legal-form-id:1}")
    private Integer fineractLegalFormId;

    @Value("${integration.fineract.ssl-trust-all:false}")
    private boolean fineractSslTrustAll;

    @Value("${integration.fineract.connect-timeout-ms:10000}")
    private int fineractConnectTimeoutMs;

    @Value("${integration.fineract.read-timeout-ms:30000}")
    private int fineractReadTimeoutMs;

    /** Default savings product when opening an account (0 = first available product). */
    @Value("${integration.fineract.default-savings-product-id:0}")
    private Long fineractDefaultSavingsProductId;

    /** Default loan product when opening an account (0 = first available product). */
    @Value("${integration.fineract.default-loan-product-id:0}")
    private Long fineractDefaultLoanProductId;
}
