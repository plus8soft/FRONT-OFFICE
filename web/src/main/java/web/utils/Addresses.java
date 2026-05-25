/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.utils;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import web.entity.core.BaseAddress;
import web.repository.dict.CountryRepository;

@Configurable
public class Addresses {

    public static final String COUNTRY_US = "840";

    public static final String COUNTRY_US_CODE = "USA";
    
    @Deprecated
    public static final String COUNTRY_RU = "840"; // Deprecated: use COUNTRY_US
    
    @Deprecated
    public static final String COUNTRY_RU_CODE = "USA"; // Deprecated: use COUNTRY_US_CODE


    public static final String STAYING_TYPE = "Staying";

    public static final String RESIDENTIAL_TYPE = "Residential";

    public static final String CORRESPONDENCE_TYPE = "Correspondence";

    private static final String EMPTY_STRING = "";

    private static final String SEPARATOR_COMMA_SPACE = ", ";

    private static final String SEPARATOR_DOT_SPACE = ". ";

    @Autowired
    private CountryRepository countryRepository;

    public String formatAddress(BaseAddress address) {
        if (address == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        append(builder, EMPTY_STRING, address.getPostalCode(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getCountry() == null ? EMPTY_STRING : countryRepository.findOne(address.getCountry()).getName(),
               SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getRegionType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getRegion(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getDistrictType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getDistrict(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getCityType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getCity(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getLocalityType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getLocality(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getStreetType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getStreet(), SEPARATOR_COMMA_SPACE);
        append(builder, "Bldg.", address.getHouse(), SEPARATOR_COMMA_SPACE);
        append(builder, "Unit", address.getHousing(), SEPARATOR_COMMA_SPACE);
        append(builder, "Struct.", address.getStructure(), SEPARATOR_COMMA_SPACE);
        append(builder, "Apt.", address.getFlat(), EMPTY_STRING);
        return builder.toString().replaceFirst(",\\s*$", EMPTY_STRING);
    }

    public String formatTransferAddress(web.service.pat.Address address) {
        if (address == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        append(builder, EMPTY_STRING, address.getPostalCode(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getCountry() == null ? EMPTY_STRING :
                                      Optional.ofNullable(countryRepository.findByAlpha3(address.getCountry()))
                                              .orElse(countryRepository.findByAlpha2(address.getCountry())).getName(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getRegionType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getRegion(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getDistrictType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getDistrict(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getCityType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getCity(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getLocalityType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getLocality(), SEPARATOR_COMMA_SPACE);
        append(builder, EMPTY_STRING, address.getStreetType(), SEPARATOR_DOT_SPACE);
        append(builder, EMPTY_STRING, address.getStreet(), SEPARATOR_COMMA_SPACE);
        append(builder, "Bldg.", address.getHouse(), SEPARATOR_COMMA_SPACE);
        append(builder, "Unit", address.getHousing(), SEPARATOR_COMMA_SPACE);
        append(builder, "Apt.", address.getFlat(), EMPTY_STRING);
        return builder.toString().replaceFirst(",\\s*$", EMPTY_STRING);
    }

    private void append(StringBuilder sb, String suffix, String value, String postfix) {
        if (value != null && !value.isEmpty()) {
            sb.append(suffix).append(value).append(postfix);
        }
    }
}
