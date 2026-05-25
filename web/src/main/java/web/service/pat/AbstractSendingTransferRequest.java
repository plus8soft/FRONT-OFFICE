/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import java.math.BigDecimal;
import lombok.Data;
import web.entity.crm.Person;
import web.entity.dict.Country;
import web.entity.dict.Currency;
import web.entity.dict.PaymentSystem;
import web.entity.ps.Recipient;

@Data
public abstract class AbstractSendingTransferRequest {

    private String departmentCode;

    private PaymentSystem paymentSystem;

    private Person person;

    private Country destinationCountry;

    private web.entity.dict.Region destinationRegion;

    private Currency acceptedCurrency;

    private Currency transferCurrency;

    private BigDecimal amount;

    private Recipient recipient;

    private BigDecimal commission;

    private BigDecimal agentCommission;
}
