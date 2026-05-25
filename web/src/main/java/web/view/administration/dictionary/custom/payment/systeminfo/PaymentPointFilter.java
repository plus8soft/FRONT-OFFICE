/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.systeminfo;

import java.io.Serializable;
import lombok.Data;
import web.entity.dict.Country;
import web.entity.dict.PaymentSystem;
import web.entity.dict.Region;

@Data
public class PaymentPointFilter implements Serializable, Cloneable {

    private String name;

    private PaymentSystem paymentSystem;

    private Country country;

    private Region region;

    private String address;

    private boolean extendedSearch;

    @Override
    public PaymentPointFilter clone() {
        try {
            return (PaymentPointFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
