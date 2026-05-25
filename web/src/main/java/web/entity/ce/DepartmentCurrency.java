/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce;

import java.io.Serializable;
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
@Table(name = "DEPARTMENT_CURRENCIES", schema = "CE")
public class DepartmentCurrency implements Serializable {

    @Id
    @SequenceGenerator(name = "DEPARTMENT_CURRENCIES_ID_SEQ", sequenceName = "DEPARTMENT_CURRENCIES_ID_SEQ", schema = "CE", allocationSize = 20)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPARTMENT_CURRENCIES_ID_SEQ")
    @Column(name = "DEPARTMENT_CURRENCIES_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENCIES_KD")
    private Currency currency;

    @Column(name = "POSITION")
    private Integer position;
}
