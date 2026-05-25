/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.ce.order;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * External rate data model.
 * 
 * This model represents currency rate data from external rate providers.
 * Currently used as a placeholder for future integrations (ECB, Fixer.io, etc.)
 */
@Data
@AllArgsConstructor
public class ExternalRate {

    private Integer currencyPosition;

    private String currencyName;

    private BigDecimal rate;

    private Integer ratio;
}
