/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import lombok.Data;
import web.entity.dict.Country;
import web.entity.dict.Currency;
import web.entity.dict.PaymentSystem;
import web.entity.dict.Region;

@Data
public class PaymentPointFilter implements Serializable, Cloneable {

    private PaymentSystem paymentSystem;

    private Currency currency;

    private Region region;

    private Country country;

    private String address;

    @Override
    public PaymentPointFilter clone() {
        try {
            return (PaymentPointFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
