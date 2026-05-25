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
public class FineractNewLoanTerms implements Serializable {

    private Long productId;

    private BigDecimal principal;

    private Integer numberOfRepayments;

    private Integer repaymentEvery;

    /** 0=days, 1=weeks, 2=months */
    private Integer repaymentFrequencyType;

    private BigDecimal interestRatePerPeriod;

    /** 2=per month, 3=per year */
    private Integer interestRateFrequencyType;

    /** 1=equal installments, 0=equal principal */
    private Integer amortizationType;

    /** 0=declining balance, 1=flat */
    private Integer interestType;

    /** 0=daily, 1=same as repayment period */
    private Integer interestCalculationPeriodType;

    private String transactionProcessingStrategyCode;

    /** Optional label in Fineract (externalId), not the product name. */
    private String externalReference;

    public Optional<String> validate() {
        if (productId == null) {
            return Optional.of("Select a loan product.");
        }
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("Enter a loan amount greater than zero.");
        }
        if (numberOfRepayments == null || numberOfRepayments <= 0) {
            return Optional.of("Enter the number of repayments.");
        }
        if (repaymentEvery == null || repaymentEvery <= 0) {
            return Optional.of("Enter repayment frequency (every N periods).");
        }
        if (repaymentFrequencyType == null) {
            return Optional.of("Select repayment period.");
        }
        if (interestRatePerPeriod == null || interestRatePerPeriod.compareTo(BigDecimal.ZERO) < 0) {
            return Optional.of("Enter interest rate per period.");
        }
        if (interestRateFrequencyType == null) {
            return Optional.of("Select interest rate frequency.");
        }
        if (amortizationType == null) {
            return Optional.of("Select amortization type.");
        }
        if (interestType == null) {
            return Optional.of("Select interest type.");
        }
        if (interestCalculationPeriodType == null) {
            return Optional.of("Select interest calculation period.");
        }
        return Optional.empty();
    }
}
