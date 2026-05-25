/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.payment;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.service.pat.AbstractSendingTransferRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class PaymentTransferRequestData extends AbstractSendingTransferRequest {

    private BigDecimal payAmount;

    private BigDecimal rate;

    private String controlNumber;
}
