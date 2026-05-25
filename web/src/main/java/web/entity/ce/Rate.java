/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.Department;
import web.entity.dict.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "CURRENCY_RATES", schema = "CE")
public class Rate implements Serializable {

    @Id
    @SequenceGenerator(name = "CURRENCY_RATES_ID_SEQ", sequenceName = "CURRENCY_RATES_ID_SEQ", schema = "CE", allocationSize = 20)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CURRENCY_RATES_ID_SEQ")
    @Column(name = "CURRENCY_RATES_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENCIES_KD")
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDERS_ID")
    private Order order;

    @Column(name = "SELL_RATE")
    private BigDecimal sellRate;

    @Column(name = "BUY_RATE")
    private BigDecimal buyRate;

    @Column(name = "EXTERNAL_RATE")
    private BigDecimal externalRate;

    @Column(name = "RATIO")
    private Integer ratio;

    @Column(name = "RULES_NAME")
    private String ruleName;

    @Column(name = "MIN_VAL")
    private BigDecimal min;

    @Column(name = "MAX_VAL")
    private BigDecimal max;

    @Column(name = "RULES_POSITION")
    private Integer rulePosition;

    @Column(name = "CURRENCIES_POSITION")
    private Integer currencyPosition;

    @Column(name = "ARM_CURRENCIES_POSITION")
    private Integer operationCurrencyPosition;
}
