/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.deposit;

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

@Table(name = "fr_pClientDepositList_out", index = "XPKfr_pClientDepositList_out")
@Data
public class OutputDepositInfo implements Serializable {

    @Column(name = "DepositId", converter = BigDecimalToLongConverter.class)
    private Long id;

    @Column(name = "BankProductName", converter = StringConverter.class)
    private String productName;

    @Column(name = "Prcnt")
    private Double interestRate;

    @Column(name = "DepositNumber", converter = StringConverter.class)
    private String number;

    @Column(name = "DateOpen", converter = TimestampToLocalDateConverter.class)
    private LocalDate dateOpen;

    @Column(name = "DateEnd", converter = TimestampToLocalDateConverter.class)
    private LocalDate dateClose;

    @Column(name = "RestAccount")
    private BigDecimal balance;

    @Column(name = "Currency", converter = {StringConverter.class, ShortToCurrencyConverter.class})
    private Currency currency;
}
