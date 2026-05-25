/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.address;

import web.entity.core.BaseAddress;

public class Address extends BaseAddress {

    public Address(BaseAddress abstractAddress) {
        super(abstractAddress);
    }

    @SuppressWarnings("checkstyle:all")
    public Address(String postalCode, String country, String regionType, String region, String districtType, String district, String cityType,
                   String city, String localityType, String locality, String streetType, String street, String house, String housing,
                   String structure, String flat, String code) {
        super(postalCode, country, regionType, region, districtType, district, cityType, city, localityType, locality, streetType, street, house,
              housing, structure, flat, code);
    }
}
