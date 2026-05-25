/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.payment;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.service.pat.AbstractSendingTransferResponse;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentTransferData extends AbstractSendingTransferResponse {

    private BigDecimal commission;

    private BigDecimal agentCommission;

    private BigDecimal rate;

    private Integer status;
}
