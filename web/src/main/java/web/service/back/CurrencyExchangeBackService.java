/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import web.entity.ce.Rate;
import web.entity.log.OperationCode;
import web.repository.back.BackException;
import web.repository.back.ce.OutputOperationData;

/**
 * Stub back-service for currency-exchange operations.
 *
 * <p>Each method throws {@link BackException} with {@link BackIntegrationMessages#CORE_NOT_CONNECTED}.
 * Replace the bodies with calls to your own core (REST / SOAP / JDBC / message broker)
 * to make the UI actually post operations. Keep the method signatures so the rest
 * of the codebase keeps compiling.
 */
@Service
@Log4j2
public class CurrencyExchangeBackService {

    public BigDecimal receiveAccountRest(String userName, Long departmentId, String accountNumber, LocalDate operationDay) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public boolean isWorkday(String userName, Long departmentId, LocalDate date) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public void installRates(String userName, List<Rate> rates) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public Long processOperation(String userName, Long personId, Long departmentId, OperationCode operationCode, LocalDateTime localDateTime,
                                 BigDecimal rate, String debetAccount, BigDecimal debetSum, String debetCurrencyId, String creditAccount,
                                 BigDecimal creditSum, String creditCurrencyId) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }

    public OutputOperationData receiveOperationData(String userName, Long departmentId, Long operationId, OperationCode operationCode,
                                                    LocalDateTime operationDate) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }
}
