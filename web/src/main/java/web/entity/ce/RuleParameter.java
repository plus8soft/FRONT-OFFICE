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
@Table(name = "RULE_PARAMS", schema = "CE")
public class RuleParameter implements Serializable {

    @Id
    @SequenceGenerator(name = "RULE_PARAMS_ID_SEQ", sequenceName = "RULE_PARAMS_ID_SEQ", schema = "CE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RULE_PARAMS_ID_SEQ")
    @Column(name = "RULE_PARAMS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RULES_ID")
    private Rule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENCIES_KD")
    private Currency currency;

    @Column(name = "SELL_RULES_SIGN")
    private Sign sellSign;

    @Column(name = "SELL_RULES_VAL")
    private BigDecimal sellValue;

    @Column(name = "SELL_RULES_PERCENT")
    private BigDecimal sellPercent;

    @Column(name = "BUY_RULES_SIGN")
    private Sign buySign;

    @Column(name = "BUY_RULES_VAL")
    private BigDecimal buyValue;

    @Column(name = "BUY_RULES_PERCENT")
    private BigDecimal buyPercent;

    @Column(name = "ENABLE")
    private Boolean enabled;
}
