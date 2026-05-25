/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.get;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import web.entity.crm.Person;
import web.entity.dict.Currency;
import web.entity.dict.PaymentSystem;

@Data
public abstract class AbstractPayoutTransfer implements Serializable {

    private PaymentSystem paymentSystem;

    private String departmentCode;

    private String controlNumber;

    private BigDecimal amount;

    private Currency currency;

    private Person person;
}
