/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import web.entity.dict.Currency;
import web.entity.log.Operation;
import web.entity.log.OperationStatus;

@Getter
@Setter
@Entity
@DiscriminatorValue("CE")
@PrimaryKeyJoinColumn(name = "CURRENCY_OPERATIONS_ID")
@Table(name = "CURRENCY_OPERATIONS", schema = "CE")
public class CurrencyOperation extends Operation {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDERS_ID")
    private Order order;

    @Column(name = "OPR_NUM")
    private Long number;

    @Column(name = "REG_NUM")
    private Long registryNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENCIES_KD")
    private Currency currency;

    @Column(name = "EXC_RATIO")
    private Integer ratio;

    @Column(name = "CUR_SUM")
    private BigDecimal sum;

    @Column(name = "BASE_AMOUNT")
    private BigDecimal baseAmount; // Amount in base currency

    @Column(name = "EXC_RATE")
    private BigDecimal rate;

    @Column(name = "EXC_EXTERNAL_RATE")
    private BigDecimal externalRate;

    @Column(name = "EXC_COMMISSION_SUM")
    private BigDecimal commission;

    @Column(name = "EXC_COMMISSION_DIS")
    private boolean commissionEnabled;

    @PrePersist
    private void prePersist() {
        setStatus(OperationStatus.COMPLETED);
    }
}
