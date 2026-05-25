/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import java.math.BigDecimal;
import lombok.Data;
import web.entity.crm.Person;
import web.entity.dict.Currency;
import web.entity.dict.PaymentSystem;

@Data
public abstract class AbstractReceivingTransferRequest {

    private Person person;

    private Currency currency;

    private BigDecimal amount;

    private PaymentSystem paymentSystem;

    private String departmentCode;

    private String controlNumber;
}
