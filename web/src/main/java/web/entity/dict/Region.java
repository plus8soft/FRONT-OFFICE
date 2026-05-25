/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@EqualsAndHashCode(of = "id")
@Builder
@Entity(name = "dict.Region")
@Table(schema = "DICT", name = "REGIONS")
public class Region implements Serializable {

    @Id
    @SequenceGenerator(name = "REGIONS_ID_SEQ", sequenceName = "REGIONS_ID_SEQ", schema = "DICT", allocationSize = 20)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REGIONS_ID_SEQ")
    @Column(name = "REGIONS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRIES_ISO")
    private Country country;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CODE")
    private String code;

    @Column(name = "ENABLE")
    private boolean enabled;

    @OneToMany(mappedBy = "region")
    private List<PaymentPoint> paymentPoints = new ArrayList<>();
}
