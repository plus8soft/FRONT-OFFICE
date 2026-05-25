/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.payment.commission;

import java.math.BigDecimal;
import lombok.Data;

/**
 * Universal commission calculation response.
 * Used for payment system commission calculations.
 */
@Data
public class CommissionResponse {

    /**
     * Transfer amount.
     */
    private BigDecimal amount;

    /**
     * Payment system commission.
     */
    private BigDecimal commission;

    /**
     * Agent commission.
     */
    private BigDecimal agentCommission;

    /**
     * Exchange rate (if currency conversion is required).
     */
    private BigDecimal rate;
}
