/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.stubs.addressstub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.entity.crm.Address;
import web.entity.dict.Level;
import web.entity.dict.address.AbstractAddressElement;
import web.entity.dict.address.City;
import web.entity.dict.address.District;
import web.entity.dict.address.Locality;
import web.entity.dict.address.Region;
import web.entity.dict.address.Street;
import web.repository.dict.address.CityRepository;
import web.repository.dict.address.DistrictRepository;
import web.repository.dict.address.LocalityRepository;
import web.repository.dict.address.RegionRepository;
import web.repository.dict.address.StreetRepository;

/**
 * Service for address autocomplete provider.
 *
 * Uses dictionary tables:
 *  - DICT.ADDRESS_ELEMENTS (regions, districts, cities, localities)
 *  - DICT.ADDRESS_STREETS (streets)
 *
 * Data is loaded from the database via JPA repositories (no hardcoded stub data).
 */
@Service
public class AddressService {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private LocalityRepository localityRepository;

    @Autowired
    private StreetRepository streetRepository;

    /**
     * Returns autocomplete suggestions by name and level.
     *
     * Data is loaded from:
     *  - DICT.ADDRESS_ELEMENTS (Region / District / City / Locality)
     *  - DICT.ADDRESS_STREETS (Street)
     *
     * @param name  name substring (LIKE %name%)
     * @param level address level (REGION, CITY, STREET, etc.)
     * @param code  parent element code (currently unused)
     */
    public List<Address> complete(String name, Level level, String code) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String search = "%" + name.toLowerCase() + "%";
        List<Address> result = new ArrayList<>();

        switch (level) {
            case REGION:
                regionRepository.findAll((root, query, cb) ->
                                cb.like(cb.lower(root.get("name")), search))
                        .forEach(element -> result.add(fromElement(element)));
                break;

            case CITY:
                cityRepository.findAll((root, query, cb) ->
                                cb.like(cb.lower(root.get("name")), search))
                        .forEach(element -> result.add(fromElement(element)));
                break;

            case DISTRICT:
                districtRepository.findAll((root, query, cb) ->
                                cb.like(cb.lower(root.get("name")), search))
                        .forEach(element -> result.add(fromElement(element)));
                break;

            case LOCALITY:
                localityRepository.findAll((root, query, cb) ->
                                cb.like(cb.lower(root.get("name")), search))
                        .forEach(element -> result.add(fromElement(element)));
                break;

            case STREET:
                streetRepository.findAll((root, query, cb) ->
                                cb.like(cb.lower(root.get("name")), search))
                        .forEach(street -> result.add(fromStreet(street)));
                break;

            default:
                break;
        }

        return result;
    }

    /**
     * Finds an address by street code.
     *
     * Looks up a row in DICT.ADDRESS_STREETS and maps it to CRM.Address.
     */
    public Address findByStreetCode(String code) {
        if (code == null) {
            return null;
        }

        Street street = streetRepository.findOne(code);
        return street != null ? fromStreet(street) : null;
    }

    /**
     * Maps a row from DICT.ADDRESS_ELEMENTS / DICT.ADDRESS_STREETS to CRM.Address.
     *
     * In AddressAutoComplete, StringValueConverters (region/city/locality/street) read
     * from address.getRegion(), so the element name is stored in region for display.
     */
    private Address fromElement(AbstractAddressElement element) {
        Address address = new Address();
        address.setCode(element.getCode());
        address.setPostalCode(element.getPostalCode());

        // For dropdowns, type and name go into Region/RegionType
        address.setRegionType(element.getType());
        address.setRegion(element.getName());
        return address;
    }

    /**
     * Street-specific mapping that also fills street and streetType.
     */
    private Address fromStreet(Street street) {
        Address address = fromElement(street);
        address.setStreetType(street.getType());
        address.setStreet(street.getName());
        return address;
    }
}
