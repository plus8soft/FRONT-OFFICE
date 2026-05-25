/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import web.repository.back.BackException;

/**
 * Stub back-service for money transfers (send / receive).
 *
 * <p>Throws {@link BackException} with {@link BackIntegrationMessages#CORE_NOT_CONNECTED}.
 * Replace the body with a call to your core to actually post a transfer.
 */
@Service
@Log4j2
public class TransferBackService {

    public Long processTransfer(String userName, Long clientId, Long departmentId, LocalDateTime date, BigDecimal amount, BigDecimal bankCommission,
                                BigDecimal paymentSystemCommission, String currencyIso, String paymentSystemName, String comment,
                                TransferType transferType, DirectionType directionType, RecipientType recipientType) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }
}
