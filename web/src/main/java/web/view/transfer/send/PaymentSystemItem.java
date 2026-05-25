/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.dict.PaymentSystem;

@Getter
@Setter
@EqualsAndHashCode(of = "paymentSystem")
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSystemItem implements Serializable {

    private boolean enabled;

    private PaymentSystem paymentSystem;

    private String departmentCode;

    private AbstractPaymentSystemFee paymentSystemFee;
}
