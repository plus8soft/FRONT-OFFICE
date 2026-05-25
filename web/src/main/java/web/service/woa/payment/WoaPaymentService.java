/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.woa.payment;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.configuration.Settings;
import web.entity.core.User;
import web.repository.back.BackException;
import web.service.back.BackIntegrationMessages;
import web.service.back.WoaPaymentBackService;
import web.service.ce.CurrencyExchangeService;
import web.view.woa.payment.WoaPayment;

@Service
public class WoaPaymentService {

    private static final String NATIONAL_CURRENCY_CODE = "840"; // USD

    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    private WoaPaymentBackService woaPaymentBackService;

    @Autowired
    private Settings settings;

    public Long transferPayment(User user, WoaPayment payment, LocalDateTime time) {
        if (!settings.isBackEnabled()) {
            throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
        }
        int type = -1;
        switch (payment.getPaymentOperationCode()) {
            case COMPANY_PARTNER_PAYMENT:
                type = 0;
                break;
            case COMPANY_FREE_PAYMENT:
                type = 1;
                break;
            case PUBLIC_SECTOR_PAYMENT:
            case TAXES_PAYMENT:
                type = 2;
                break;
        }
        Long externalId = null;
        if (payment.getPerson() != null) {
            externalId = payment.getPerson().getExternalId();
        }
        return woaPaymentBackService.transferPayment(user, externalId, 1L, time.toLocalDate(), NATIONAL_CURRENCY_CODE,
                                                     currencyExchangeService.findCash(user, NATIONAL_CURRENCY_CODE).getId(),
                                                     payment.getAccount().getId().startsWith("40911") ? payment.getAccount().getId() :
                                                     "40911000000000000000", payment.getSum(),
                                                     payment.getPayAction() != null ? payment.getPayAction().getCashSymbol() : null,
                                                     payment.getPurpose(), "70601000000000000000", payment.getCounteragentCommission(), "1", type)
                                    .getTransactId();
    }
}
