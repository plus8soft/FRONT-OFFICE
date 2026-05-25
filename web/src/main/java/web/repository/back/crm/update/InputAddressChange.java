/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.converter.CountryAlpha2ToIsoConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pChangeClientF_ModifyAddress2_in", index = "XPKfr_pChangeClientF_ModifyAddress2_in")
@AllArgsConstructor
@Data
public class InputAddressChange {

    @Column(name = "AddressTypeBrief")
    private String type;

    @Column(name = "PostIndex")
    private String postalCode;

    @Column(name = "CountryCode", converter = CountryAlpha2ToIsoConverter.class)
    private String countryCode;

    @Column(name = "RegionKind")
    private String regionType;

    @Column(name = "RegionCode")
    private String regionCode;

    @Column(name = "AreaKind")
    private String districtType;

    @Column(name = "AreaCode")
    private String districtCode;

    @Column(name = "CityKind")
    private String cityType;

    @Column(name = "CityCode")
    private String cityCode;

    @Column(name = "TownKind")
    private String localityType;

    @Column(name = "TownCode")
    private String localityCode;

    @Column(name = "StreetKind")
    private String streetType;

    @Column(name = "StreetCode")
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
