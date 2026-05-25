/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.credit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import web.entity.dict.Currency;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.converter.ShortToCurrencyConverter;
import web.repository.back.converter.StringConverter;
import web.repository.back.converter.TimestampToLocalDateConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientCreditList_out", index = "XPKfr_pClientCreditList_out")
@Data
public class OutputCreditInfo implements Serializable {

    @Column(name = "CreditID", converter = BigDecimalToLongConverter.class)
    private Long id;

    @Column(name = "BankProductName", converter = StringConverter.class)
    private String productName;

    @Column(name = "Prcnt")
    private Double interestRate;

    @Column(name = "CreditNumber", converter = StringConverter.class)
    private String number;

    @Column(name = "DateEnd", converter = TimestampToLocalDateConverter.class)
    private LocalDate dateClose;

    @Column(name = "CurrentDebtOnLoan")
    private BigDecimal debt;

    @Column(name = "NextPayAmount")
    private BigDecimal payAmount;

    @Column(name = "Currency", converter = {StringConverter.class, ShortToCurrencyConverter.class})
    private Currency currency;
}
