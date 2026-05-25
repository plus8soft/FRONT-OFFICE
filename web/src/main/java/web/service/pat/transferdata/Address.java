/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import web.jaxb.StringTrimAdapter;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Address {

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String postalCode;

    private String country;

    private String countryCode;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String regionType;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String region;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String districtType;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String district;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String cityType;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String city;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String localityType;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String locality;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String streetType;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String street;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String house;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String housing;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String flat;
}
