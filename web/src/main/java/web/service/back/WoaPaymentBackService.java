/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import web.entity.core.User;
import web.repository.back.BackException;
import web.repository.back.woa.payment.OutputTransfer;

/**
 * Stub back-service for WOA (counterparty / one-off) payments.
 *
 * <p>Throws {@link BackException} with {@link BackIntegrationMessages#CORE_NOT_CONNECTED}.
 * Replace the body with a call to your core to actually post the payment.
 */
@Service
@Log4j2
public class WoaPaymentBackService {

    public OutputTransfer transferPayment(User user, Long clientId, Long documentId, LocalDate date, String currencyId, String accBriefDeb,
                                          String accBriefCre, BigDecimal summ, String cashSymbol, String reason, String accBriefDoh,
                                          BigDecimal commisionSumm, String commentCom, int paymentType) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }
}
