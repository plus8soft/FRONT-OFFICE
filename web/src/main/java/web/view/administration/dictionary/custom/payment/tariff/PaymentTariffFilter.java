/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.tariff;

import java.io.Serializable;
import lombok.Data;
import web.entity.dict.PaymentSystem;

@Data
public class PaymentTariffFilter implements Serializable, Cloneable {

    private String name;

    private PaymentSystem paymentSystem;

    private Boolean status;

    private Boolean destinationRequired;

    private boolean extendedSearch;

    @Override
    public PaymentTariffFilter clone() {
        try {
            return (PaymentTariffFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
