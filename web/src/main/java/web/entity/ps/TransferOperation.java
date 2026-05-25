/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ps;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import web.entity.dict.Currency;
import web.entity.dict.PaymentSystem;
import web.entity.log.Operation;

@Getter
@Setter
@Entity
@DiscriminatorValue("PS")
@PrimaryKeyJoinColumn(name = "TRANSFER_OPERATIONS_ID")
@Table(name = "TRANSFER_OPERATIONS", schema = "PS")
public class TransferOperation extends Operation {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PS_SYSTEMS_NAME")
    private PaymentSystem paymentSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECIPIENTS_ID")
    private Recipient recipient;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENCY")
    private Currency currency;

    @Column(name = "TRANSFER_AMOUNT")
    private BigDecimal transferAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSFER_CURRENCY")
    private Currency transferCurrency;

    @Column(name = "DESTINATION_COUNTRY")
    private String country;

    @Column(name = "DESTINATION_REGION")
    private String region;

    @Column(name = "DESTINATION_CITY")
    private String city;

    @Column(name = "DESTINATION_POINT")
    private String point;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BANK_COMMISSION_CURRENCY")
    private Currency bankCommissionCurrency;

    @Column(name = "BANK_COMMISSION_AMOUNT")
    private BigDecimal bankCommissionAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SYSTEM_COMMISSION_CURRENCY")
    private Currency systemCommissionCurrency;

    @Column(name = "SYSTEM_COMMISSION_AMOUNT")
    private BigDecimal systemCommissionAmount;

    @Column(name = "EXC_RATIO")
    private Long ratio;

    @Column(name = "EXC_RATE")
    private BigDecimal rate;

    @Column(name = "TRANSFER_NUMBER")
    private String number;
}
