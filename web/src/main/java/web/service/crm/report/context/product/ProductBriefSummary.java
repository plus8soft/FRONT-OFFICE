/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.product;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.repository.back.crm.credit.OutputCreditInfo;
import web.repository.back.crm.deposit.OutputDepositInfo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBriefSummary {

    private Instant date;

    private List<OutputCreditInfo> credits = new ArrayList<>();

    private List<OutputDepositInfo> deposits = new ArrayList<>();
}
