/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.woa.payment;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@Log4j2
public class StepTwoView implements Serializable {

    private WoaPayment payment;

    public void init(WoaPayment payment) {
        this.payment = payment;
    }
}
