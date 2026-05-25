/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
@Table(name = "RULES", schema = "CE")
public class Rule implements Serializable {

    @Id
    @SequenceGenerator(name = "RULES_ID_SEQ", sequenceName = "RULES_ID_SEQ", schema = "CE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RULES_ID_SEQ")
    @Column(name = "RULES_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ENABLE")
    private boolean enabled;

    @Column(name = "SYSTEM_NAME")
    private String systemName;

    @Column(name = "MIN_VAL")
    private BigDecimal min;

    @Column(name = "MAX_VAL")
    private BigDecimal max;

    @Column(name = "IS_SYSTEM")
    private boolean system;

    @Column(name = "IS_CURRENCY")
    private boolean currency;

    @Column(name = "IS_COMMISION")
    private boolean commision;

    @Column(name = "POSITION")
    private Integer position;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.REMOVE)
    private List<RuleParameter> ruleParameters = new ArrayList<>();
}
