/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.systeminfo;

import java.io.Serializable;
import lombok.Data;
import web.entity.dict.Country;
import web.entity.dict.PaymentSystem;

@Data
public class RegionFilter implements Serializable, Cloneable {

    private String name;

    private Country country;

    private Boolean status;

    private PaymentSystem paymentSystem;

    private boolean extendedSearch;

    @Override
    public RegionFilter clone() {
        try {
            return (RegionFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
