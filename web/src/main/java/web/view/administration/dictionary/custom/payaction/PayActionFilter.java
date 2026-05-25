/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payaction;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import web.entity.dict.PayActionType;

@Getter
@Setter
@Data
public class PayActionFilter implements Serializable, Cloneable {

    private String name;

    private PayActionType type;

    private String cashSymbol;

    private Boolean disabled;

    private boolean extendedSearch;

    @Override
    public PayActionFilter clone() {
        try {
            return (PayActionFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
