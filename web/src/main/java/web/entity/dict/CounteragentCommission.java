/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.math.BigDecimal;
import java.time.LocalDate;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(schema = "DICT", name = "COUNTERAGENT_COMMISSIONS")
public class CounteragentCommission {

    @Id
    @SequenceGenerator(name = "COUNTERAGENT_COMMISSIONS_ID_SEQ", sequenceName = "COUNTERAGENT_COMMISSIONS_ID_SEQ", schema = "DICT",
                       allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COUNTERAGENT_COMMISSIONS_ID_SEQ")
    @Column(name = "COUNTERAGENT_COMMISSIONS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTERAGENTS_ID")
    private Counteragent counteragent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTERAGENT_PAY_ACTIONS_ID")
    private CounteragentPayAction counteragentPayAction;

    @Column(name = "START_DATE")
    private LocalDate date;

    @Column(name = "VALUE_RANGE")
    private BigDecimal valueRange;

    @Column(name = "PERCENTAGE")
    private BigDecimal percentage;

    @Column(name = "FIXED_VALUE")
    private BigDecimal fixed;

    @Column(name = "MIN_VAL")
    private BigDecimal min;

    @Column(name = "MAX_VAL")
    private BigDecimal max;
}
