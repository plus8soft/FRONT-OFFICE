/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

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
import web.entity.dict.PaymentSystem;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "DEPARTMENT_PS_SYSTEMS", schema = "CORE")
public class DepartmentPaymentSystem implements Serializable {

    @Id
    @SequenceGenerator(name = "DEPARTMENT_PS_SYSTEMS_PK_SEQ", sequenceName = "DEPARTMENT_PS_SYSTEMS_PK_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPARTMENT_PS_SYSTEMS_PK_SEQ")
    @Column(name = "DEPARTMENT_PS_SYSTEMS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PS_SYSTEMS_NAME")
    private PaymentSystem paymentSystem;

    @Column(name = "CODE")
    private String code;

    @Column(name = "CAN_SEND")
    private Boolean send;

    @Column(name = "CAN_PAYOUT")
    private Boolean payOut;
}
