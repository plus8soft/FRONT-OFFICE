/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.converter.CountryAlpha2ToIsoConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientCreateF_CrtAddress_in", index = "XPKfr_pClientCreateF_CrtAddress_in")
@AllArgsConstructor
@Data
public class InputAddressCreation {

    @Column(name = "AddressType")
    private String type;

    @Column(name = "PostIndex")
    private String postalCode;

    @Column(name = "CountryCodeValue", converter = CountryAlpha2ToIsoConverter.class)
    private String countryCode;

    @Column(name = "RegionType")
    private String regionType;

    @Column(name = "RegionCodeValue")
    private String regionCode;

    @Column(name = "AreaType")
    private String districtType;

    @Column(name = "AreaCodeValue")
    private String districtCode;

    @Column(name = "CityType")
    private String cityType;

    @Column(name = "CityCodeValue")
    private String cityCode;

    @Column(name = "TownType")
    private String localityType;

    @Column(name = "TownCodeValue")
    private String localityCode;

    @Column(name = "StreetType")
    private String streetType;

    @Column(name = "StreetCodeValue")
    private String street;

    @Column(name = "House")
    private String house;

    @Column(name = "Frame")
    private String housing;

    @Column(name = "Construction")
    private String structure;

    @Column(name = "Flat")
    private String flat;

    @Column(name = "Name")
    private String fullName;
}
