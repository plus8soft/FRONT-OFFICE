/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.ps.payment;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import web.service.crm.report.context.ps.AbstractPaymentContext;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentTransferContext extends AbstractPaymentContext {

    private BigDecimal exchangeRate;

    private String senderCitizenship;

    private String receiverCitizenship;
}
