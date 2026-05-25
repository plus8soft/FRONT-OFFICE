/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FineractEnumOptions {

    private FineractEnumOptions() {
    }

    public static List<FineractEnumOption> repaymentFrequencyTypes() {
        return Collections.unmodifiableList(Arrays.asList(
                new FineractEnumOption(0, "Days"),
                new FineractEnumOption(1, "Weeks"),
                new FineractEnumOption(2, "Months")
        ));
    }

    public static List<FineractEnumOption> interestRateFrequencyTypes() {
        return Collections.unmodifiableList(Arrays.asList(
                new FineractEnumOption(2, "Per month"),
                new FineractEnumOption(3, "Per year")
        ));
    }

    public static List<FineractEnumOption> amortizationTypes() {
        return Collections.unmodifiableList(Arrays.asList(
                new FineractEnumOption(1, "Equal installments"),
                new FineractEnumOption(0, "Equal principal payments")
        ));
    }

    public static List<FineractEnumOption> interestTypes() {
        return Collections.unmodifiableList(Arrays.asList(
                new FineractEnumOption(0, "Declining balance"),
                new FineractEnumOption(1, "Flat")
        ));
    }

    public static List<FineractEnumOption> interestCalculationPeriodTypes() {
        return Collections.unmodifiableList(Arrays.asList(
                new FineractEnumOption(0, "Daily"),
                new FineractEnumOption(1, "Same as repayment period")
        ));
    }
}
