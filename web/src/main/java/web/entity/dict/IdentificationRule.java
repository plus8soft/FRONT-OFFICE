/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

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
import web.entity.core.Task;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(schema = "DICT", name = "IDENTIFICATION_RULES")
public class IdentificationRule implements Serializable {

    @Id
    @SequenceGenerator(name = "IDENTIFICATION_RULES_ID_SEQ", sequenceName = "IDENTIFICATION_RULES_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IDENTIFICATION_RULES_ID_SEQ")
    @Column(name = "IDENTIFICATION_RULES_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASKS_ID")
    private Task task;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SYSTEM_NAME")
    private String systemName;

    @Column(name = "MIN_VAL")
    private BigDecimal min;

    @Column(name = "MAX_VAL")
    private BigDecimal max;
}
