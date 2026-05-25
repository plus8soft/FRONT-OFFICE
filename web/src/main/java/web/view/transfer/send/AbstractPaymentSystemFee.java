/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractPaymentSystemFee {

    private BigDecimal conversion;

    private BigDecimal paymentSystemCommission;

    private BigDecimal bankCommission;

    private BigDecimal sum;

    private BigDecimal commission;
}
