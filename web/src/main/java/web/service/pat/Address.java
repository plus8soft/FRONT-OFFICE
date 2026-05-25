/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import java.io.Serializable;
import lombok.Data;

@Data
public class Address implements Serializable {

    private String postalCode;

    private String country;

    private String countryCode;

    private String regionType;

    private String region;

    private String districtType;

    private String district;

    private String cityType;

    private String city;

    private String localityType;

    private String locality;

    private String streetType;

    private String street;

    private String house;

    private String housing;

    private String flat;
}
