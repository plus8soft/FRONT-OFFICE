/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.DepartmentPaymentSystem;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(schema = "DICT", name = "PS_SYSTEMS")
public class PaymentSystem implements Serializable {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "SYSTEMS_NAME")
    private PaymentSystemName id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "AGENT_COMMISSION")
    private BigDecimal commission;

    @Column(name = "ENABLE")
    private boolean enabled;

    @OneToMany(mappedBy = "paymentSystem", cascade = CascadeType.REMOVE)
    private List<PaymentPoint> paymentPoints = new ArrayList<>();

    @OneToMany(mappedBy = "paymentSystem")
    private List<DepartmentPaymentSystem> departmentPaymentSystems = new ArrayList<>();
}
