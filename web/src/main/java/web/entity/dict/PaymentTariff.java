/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(schema = "DICT", name = "PS_TARIFFS")
public class PaymentTariff implements Serializable {

    @Id
    @SequenceGenerator(name = "PS_TARIFFS_ID_SEQ", sequenceName = "PS_TARIFFS_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PS_TARIFFS_ID_SEQ")
    @Column(name = "PS_TARIFFS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SYSTEMS_NAME")
    private PaymentSystem paymentSystem;

    @Column(name = "CODE")
    private String code;

    @Column(name = "NAME")
    private String name;

    @Column(name = "IS_DESTINATION_REQUIRED")
    private Boolean destinationRequired;

    @Column(name = "ENABLE")
    private boolean enabled;
}
