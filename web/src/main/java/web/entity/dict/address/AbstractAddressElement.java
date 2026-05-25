/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict.address;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "code")
@MappedSuperclass
public abstract class AbstractAddressElement implements LeveledAddressElement, Serializable {

    @Id
    @Column(name = "CODE")
    private String code;

    @Column(name = "SOCR")
    private String type;

    @Column(name = "NAME")
    private String name;

    @Column(name = "IND")
    private String postalCode;

    @Column(name = "REGION", insertable = false, updatable = false)
    private String region;

    @Column(name = "DISTRICT", insertable = false, updatable = false)
    private String district;

    @Column(name = "CITY", insertable = false, updatable = false)
    private String city;

    @Column(name = "LOCALITY", insertable = false, updatable = false)
    private String locality;

    @Column(name = "ACTUALITY", insertable = false, updatable = false)
    private String actuality;
}
