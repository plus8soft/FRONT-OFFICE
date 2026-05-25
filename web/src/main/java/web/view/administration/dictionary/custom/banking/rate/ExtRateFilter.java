/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.rate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import web.entity.dict.Currency;

@Getter
@Setter
@Data
public class ExtRateFilter implements Serializable, Cloneable {

    private Currency currency;

    private LocalDateTime date;

    private Integer ratio;

    private BigDecimal sellRate;

    private BigDecimal buyRate;

    @Override
    public ExtRateFilter clone() {
        try {
            return (ExtRateFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
