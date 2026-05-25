/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.product.credit;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.entity.dict.Currency;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credit {

    private String name;

    private Double interestRate;

    private String number;

    private LocalDate dateOpen;

    private LocalDate dateClose;

    private BigDecimal debt;

    private BigDecimal payAmount;

    private Currency currency;
}
