/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
@Entity
@Table(schema = "DICT", name = "COUNTRIES")
public class Country implements Serializable {

    @Id
    @Column(name = "COUNTRIES_ISO")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ALPHA2")
    private String alpha2;

    @Column(name = "ALPHA3")
    private String alpha3;

    @Column(name = "ENABLE")
    private boolean enabled;

    @OneToMany(mappedBy = "country")
    private List<PaymentPoint> paymentPoints = new ArrayList<>();
}
