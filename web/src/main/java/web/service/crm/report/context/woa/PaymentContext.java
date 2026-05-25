/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.woa;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.entity.dict.Bank;
import web.entity.dict.Counteragent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentContext {

    private String senderEin;

    private BigDecimal total;

    private String payer;

    private String payerAccount;

    private Bank counteragentBank;

    private Counteragent counteragent;

    private String counteragentAccount;

    private String purpose;
}
