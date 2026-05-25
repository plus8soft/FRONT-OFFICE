/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.payment;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.entity.crm.Person;
import web.service.pat.AbstractReceivingConfirmRequest;
import web.service.pat.Receiver;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReceivingConfirmData extends AbstractReceivingConfirmRequest {

    private String controlNumber;

    private String destinationCountryCode;

    private String region;

    private String withdrawCurrencyIso;

    private BigDecimal amount;

    private BigDecimal commission;

    private BigDecimal agentCommission;

    private web.service.pat.Sender sender;

    private Receiver receiver;

    private Person person;
}
