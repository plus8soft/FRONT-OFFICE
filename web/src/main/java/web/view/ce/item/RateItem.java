/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.item;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.ce.Rate;
import web.entity.dict.Currency;
import web.entity.dict.ExtRate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateItem implements Serializable {

    private RuleItem ruleItem;

    private Currency currency;

    private Rate rate;

    private BigDecimal differenceSell;

    private BigDecimal differenceBuy;

    /**
     * External rate provider.
     * Currently uses stub implementation with hardcoded test data.
     * Can be replaced with actual external rate provider (ECB, Fixer.io, etc.)
     */
    private ExtRate externalRate;

    /**
     * Market rate provider.
     * Currently uses stub implementation with hardcoded test data.
     * Can be replaced with market data API integration.
     */
    private ExtRate marketRate;

    private boolean sellConflict;

    private boolean buyConflict;

    private boolean ratioConflict;
}
