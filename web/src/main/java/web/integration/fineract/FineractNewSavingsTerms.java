/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FineractNewSavingsTerms implements Serializable {

    private Long productId;

    /** Annual interest rate % (from product by default, editable for this account). */
    private BigDecimal nominalAnnualInterestRate;

    /** Optional label in Fineract (externalId). */
    private String externalReference;

    public Optional<String> validate() {
        if (productId == null) {
            return Optional.of("Select a savings product.");
        }
        if (nominalAnnualInterestRate != null && nominalAnnualInterestRate.compareTo(BigDecimal.ZERO) < 0) {
            return Optional.of("Interest rate cannot be negative.");
        }
        return Optional.empty();
    }
}
