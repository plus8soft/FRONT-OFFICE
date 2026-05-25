/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.slip;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.service.crm.report.context.ps.Currency;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditSlipContext {

    private String departmentCode;

    private String departmentName;

    private Long number;

    private LocalDateTime date;

    private BigDecimal amount;

    private Currency currency;

    private String personFirstname;

    private String personLastname;

    private String personPatronymic;
}
