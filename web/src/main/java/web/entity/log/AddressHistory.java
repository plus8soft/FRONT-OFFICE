/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.log;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.BaseAddress;
import web.entity.crm.PersonAddress;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
@AttributeOverrides({@AttributeOverride(name = "postalCode", column = @Column(name = "ADR_POSTALCODE")),
                     @AttributeOverride(name = "country", column = @Column(name = "ADR_COUNTRY")),
                     @AttributeOverride(name = "regionType", column = @Column(name = "ADR_REGIONTYPE")),
                     @AttributeOverride(name = "region", column = @Column(name = "ADR_REGION")),
                     @AttributeOverride(name = "districtType", column = @Column(name = "ADR_DISTRICTTYPE")),
                     @AttributeOverride(name = "district", column = @Column(name = "ADR_DISTRICT")),
                     @AttributeOverride(name = "cityType", column = @Column(name = "ADR_CITYTYPE")),
                     @AttributeOverride(name = "city", column = @Column(name = "ADR_CITY")),
                     @AttributeOverride(name = "localityType", column = @Column(name = "ADR_LOCALITYTYPE")),
                     @AttributeOverride(name = "locality", column = @Column(name = "ADR_LOCALITY")),
                     @AttributeOverride(name = "streetType", column = @Column(name = "ADR_STREETTYPE")),
                     @AttributeOverride(name = "street", column = @Column(name = "ADR_STREET")),
                     @AttributeOverride(name = "house", column = @Column(name = "ADR_HOUSE")),
                     @AttributeOverride(name = "housing", column = @Column(name = "ADR_HOUSING")),
                     @AttributeOverride(name = "structure", column = @Column(name = "ADR_STRUCTURE")),
                     @AttributeOverride(name = "flat", column = @Column(name = "ADR_FLAT")),
                     @AttributeOverride(name = "code", column = @Column(name = "ADR_ADDRESS_CODE"))})
public class AddressHistory extends BaseAddress implements Serializable {

    public AddressHistory(PersonAddress personAddress) {
        super(personAddress.getAddress());
    }
}
