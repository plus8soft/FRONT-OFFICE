/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseAddress implements Serializable {

    @Column(name = "POSTALCODE")
    private String postalCode;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "REGIONTYPE")
    private String regionType;

    @Column(name = "REGION")
    private String region;

    @Column(name = "DISTRICTTYPE")
    private String districtType;

    @Column(name = "DISTRICT")
    private String district;

    @Column(name = "CITYTYPE")
    private String cityType;

    @Column(name = "CITY")
    private String city;

    @Column(name = "LOCALITYTYPE")
    private String localityType;

    @Column(name = "LOCALITY")
    private String locality;

    @Column(name = "STREETTYPE")
    private String streetType;

    @Column(name = "STREET")
    private String street;

    @Column(name = "HOUSE")
    private String house;

    @Column(name = "HOUSING")
    private String housing;

    @Column(name = "STRUCTURE")
    private String structure;

    @Column(name = "FLAT")
    private String flat;

    /**
     * Address code for international address identification.
     */
    @Column(name = "ADDRESS_CODE")
    private String code;

    protected BaseAddress(BaseAddress abstractAddress) {
        postalCode = abstractAddress.getPostalCode();
        country = abstractAddress.getCountry();
        regionType = abstractAddress.getRegionType();
        region = abstractAddress.getRegion();
        districtType = abstractAddress.getDistrictType();
        district = abstractAddress.getDistrict();
        cityType = abstractAddress.getCityType();
        city = abstractAddress.getCity();
        localityType = abstractAddress.getLocalityType();
        locality = abstractAddress.getLocality();
        streetType = abstractAddress.getStreetType();
        street = abstractAddress.getStreet();
        house = abstractAddress.getHouse();
        housing = abstractAddress.getHousing();
        structure = abstractAddress.getStructure();
        flat = abstractAddress.getFlat();
        code = abstractAddress.getCode();
    }
}
