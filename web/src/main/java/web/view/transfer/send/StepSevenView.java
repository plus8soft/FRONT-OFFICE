/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@Log4j2
public class StepSevenView implements Serializable {

    private PaymentTransfer paymentTransfer;

    public void init(PaymentTransfer paymentTransfer) {
        this.paymentTransfer = paymentTransfer;
    }
}
