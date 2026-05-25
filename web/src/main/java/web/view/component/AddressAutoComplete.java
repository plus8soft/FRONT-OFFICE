/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.component;

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import web.component.StringValueConverter;
import web.entity.core.BaseAddress;
import web.entity.crm.Address;
import web.entity.dict.Abbreviation;
import web.entity.dict.Abbreviation_;
import web.entity.dict.Country;
import web.entity.dict.Level;
import web.repository.dict.AbbreviationRepository;
import web.repository.dict.CountryRepository;
import web.service.stubs.addressstub.AddressService;
import web.utils.Addresses;
import web.utils.Utils;
import web.view.converter.AutoCompletePojoConverter;

@Configurable
@Getter
@Setter
@Log4j2
public class AddressAutoComplete {

    @Autowired
    private AbbreviationRepository abbreviationRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private Utils utils;

    private String addressText;

    private BaseAddress address;

    private List<Country> countries;

    private List<Abbreviation> regions;

    private List<Abbreviation> districts;

    private List<Abbreviation> cities;

    private List<Abbreviation> localities;

    private List<Abbreviation> streets;

    private StringValueConverter<Address> regionValueConverter = new StringValueConverter<Address>() {
        @Override
        public String toTarget(Address source) {
            return source.getRegion();
        }
    };

    private StringValueConverter<Address> districtValueConverter = new StringValueConverter<Address>() {
        @Override
        public String toTarget(Address source) {
            return source.getRegion();
        }
    };

    private StringValueConverter<Address> cityValueConverter = new StringValueConverter<Address>() {
        @Override
        public String toTarget(Address source) {
            return source.getRegion();
        }
    };

    private StringValueConverter<Address> localityValueConverter = new StringValueConverter<Address>() {
        @Override
        public String toTarget(Address source) {
            return source.getRegion();
        }
    };

    private StringValueConverter<Address> streetValueConverter = new StringValueConverter<Address>() {
        @Override
        public String toTarget(Address source) {
            return source.getRegion();
        }
    };

    private AutoCompletePojoConverter<Address> converter = new AutoCompletePojoConverter<>(Collections.emptyList(), Address::getCode);

    public AddressAutoComplete(BaseAddress address) {
        this.address = address;
    }

    @PostConstruct
    private void init() {
        countries = countryRepository.findAllByOrderByNameAsc();
        regions = abbreviationRepository.findAll((root, query, cb) -> cb.equal(root.get(Abbreviation_.level), Level.REGION));
        districts = abbreviationRepository.findAll((root, query, cb) -> cb.equal(root.get(Abbreviation_.level), Level.DISTRICT));
        cities = abbreviationRepository.findAll((root, query, cb) -> cb.equal(root.get(Abbreviation_.level), Level.CITY));
        localities = abbreviationRepository.findAll((root, query, cb) -> cb.equal(root.get(Abbreviation_.level), Level.LOCALITY));
        streets = abbreviationRepository.findAll((root, query, cb) -> cb.equal(root.get(Abbreviation_.level), Level.STREET));
        addressText = utils.getAddresses().formatAddress(address);
    }

    private List<Address> complete(String name, Level level) {
        // Stub implementation: returns test addresses for any country
        // Currently returns hardcoded test data - you can implement your own API for address autocomplete
        List<Address> addresses = addressService.complete(name, level, address.getCode());
        converter.setSource(addresses);
        return addresses;
    }

    public List<Address> completeRegion(String name) {
        return complete(name, Level.REGION);
    }

    public List<Address> completeDistrict(String name) {
        return complete(name, Level.DISTRICT);
    }

    public List<Address> completeCity(String name) {
        return complete(name, Level.CITY);
    }

    public List<Address> completeLocality(String name) {
        return complete(name, Level.LOCALITY);
    }

    public List<Address> completeStreet(String name) {
        return complete(name, Level.STREET);
    }

    public void onSelectListener(SelectEvent event) {
        Address address = (Address) event.getObject();
        this.address.setCode(address.getCode());
        this.address.setPostalCode(address.getPostalCode());
        this.address.setRegionType(address.getRegionType());
        this.address.setRegion(address.getRegion());
        this.address.setDistrictType(address.getDistrictType());
        this.address.setDistrict(address.getDistrict());
        this.address.setCityType(address.getCityType());
        this.address.setCity(address.getCity());
        this.address.setLocalityType(address.getLocalityType());
        this.address.setLocality(address.getLocality());
        this.address.setStreetType(address.getStreetType());
        this.address.setStreet(address.getStreet());
    }

    public void resetCode() {
        this.address.setCode(null);
    }
}
