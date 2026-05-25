/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.search;

import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.converter.CountryAlpha2ToIsoConverter;
import web.repository.back.converter.StringConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientSearchF_SrchAddress_out", index = "XPKfr_pClientSearchF_SrchAddress_out", checkError = false)
@Data
public class OutputAddressSearch {

    @Column(name = "InstitutionID", converter = BigDecimalToLongConverter.class)
    private Long externalId;

    @Column(name = "PostIndex", converter = StringConverter.class)
    private String postalCode;

    @Column(name = "CountryCode", converter = {StringConverter.class, CountryAlpha2ToIsoConverter.class})
    private String country;

    @Column(name = "Region", converter = StringConverter.class)
    private String region;

    @Column(name = "RegionType", converter = StringConverter.class)
    private String regionType;

    @Column(name = "Area", converter = StringConverter.class)
    private String district;

    @Column(name = "AreaType", converter = StringConverter.class)
    private String districtType;

    @Column(name = "City", converter = StringConverter.class)
    private String city;

    @Column(name = "CityType", converter = StringConverter.class)
    private String cityType;

    @Column(name = "City1", converter = StringConverter.class)
    private String locality;

    @Column(name = "City1Type", converter = StringConverter.class)
    private String localityType;

    @Column(name = "Street", converter = StringConverter.class)
    private String street;

    @Column(name = "StreetType", converter = StringConverter.class)
    private String streetType;

    @Column(name = "StreetCode", converter = StringConverter.class)
    private String streetCode;

    @Column(name = "House", converter = StringConverter.class)
    private String house;

    @Column(name = "Frame", converter = StringConverter.class)
    private String housing;

    @Column(name = "Construction", converter = StringConverter.class)
    private String structure;

    @Column(name = "Flat", converter = StringConverter.class)
    private String flat;

    @Column(name = "Name", converter = StringConverter.class)
    private String text;

    @Column(name = "AddressType", converter = StringConverter.class)
    private String type;
}
