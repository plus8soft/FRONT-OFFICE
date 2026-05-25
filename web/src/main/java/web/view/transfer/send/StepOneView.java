/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.Join;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import web.entity.core.DepartmentPaymentSystem_;
import web.entity.crm.Person;
import web.entity.dict.Account;
import web.entity.dict.AccountLink;
import web.entity.dict.AccountLinkType;
import web.entity.dict.AccountLink_;
import web.entity.dict.Account_;
import web.entity.dict.Country;
import web.entity.dict.Country_;
import web.entity.dict.Currency;
import web.entity.dict.Currency_;
import web.entity.dict.PaymentPoint;
import web.entity.dict.PaymentPoint_;
import web.entity.dict.PaymentSystem_;
import web.entity.dict.Region;
import web.entity.dict.Region_;
import web.repository.core.DepartmentPaymentSystemRepository;
import web.repository.crm.AddressRepository;
import web.repository.crm.PersonRepository;
import web.repository.dict.CountryRepository;
import web.repository.dict.CurrencyRepository;
import web.repository.dict.PaymentPointRepository;
import web.repository.dict.PaymentSystemRepository;
import web.repository.dict.RegionRepository;
import web.session.UserSession;

@Getter
@Setter
@Log4j2
public class StepOneView implements Serializable {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PaymentPointRepository paymentPointRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private PaymentSystemRepository paymentSystemRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DepartmentPaymentSystemRepository departmentPaymentSystemRepository;

    @Autowired
    private UserSession userSession;

    private List<Country> countries;

    private List<Country> paymentPointsCountries;

    private List<Region> regions;

    private List<Currency> acceptedCurrencies;

    private List<Currency> transferCurrencies;

    private PaymentTransfer paymentTransfer;

    private List<PaymentSystemItem> paymentSystemItems;

    public void init(PaymentTransfer paymentTransfer, Long personId) {
        this.paymentTransfer = paymentTransfer;
        countries = countryRepository.findAll((root, query, cb) -> cb.isTrue(root.get(Country_.enabled)), new Sort(Country_.name.getName()));
        paymentPointsCountries =
                countryRepository.findAll((root, query, cb) -> cb.isNotEmpty(root.get(Country_.paymentPoints)), new Sort(Country_.name.getName()));
        acceptedCurrencies = currencyRepository.findAll((root, query, cb) -> {
            query.distinct(true);
            Join<Account, AccountLink> join = root.join(Currency_.accounts).join(Account_.accountLinks);
            return cb.and(cb.equal(join.get(AccountLink_.type), AccountLinkType.CURRENCY_EXCHANGE.name()),
                          cb.equal(join.get(AccountLink_.department), userSession.getUser().getDepartment()));
        });
        paymentSystemItems = departmentPaymentSystemRepository.findAll((root, query, cb) -> {
            root.fetch(DepartmentPaymentSystem_.paymentSystem);
            return cb.and(cb.equal(root.get(DepartmentPaymentSystem_.department), userSession.getUser().getDepartment()),
                          cb.isTrue(root.get(DepartmentPaymentSystem_.send)),
                          cb.isTrue(root.get(DepartmentPaymentSystem_.paymentSystem).get(PaymentSystem_.enabled)));
        }).stream().map(departmentPaymentSystem -> new PaymentSystemItem(false, departmentPaymentSystem.getPaymentSystem(),
                                                                         departmentPaymentSystem.getCode(), null)).collect(Collectors.toList());
        updateRegions();
        updateTransferCurrency();
        updatePaymentSystem();
        if (personId != null) {
            Person person = personRepository.findOne(personId);
            countries.stream()
                     .filter(country -> country.getId().equals(person.getCitizenship()) || country.getId().equals(person.getResidentCountry()))
                     .forEach(country -> {
                         if (country.getId().equals(person.getCitizenship())) {
                             paymentTransfer.setCitizenship(country);
                         }
                         if (country.getId().equals(person.getResidentCountry())) {
                             paymentTransfer.setResidentCountry(country);
                         }
                     });
        }
    }

    public void updatePaymentSystem() {
        if (paymentTransfer.getTransferCurrency() != null) {
            paymentSystemItems.forEach(paymentSystemItem -> paymentSystemItem.setEnabled(paymentPointRepository.exists((root, query, cb) -> cb
                    .and(cb.equal(root.get(PaymentPoint_.paymentSystem), paymentSystemItem.getPaymentSystem()),
                         cb.equal(root.get(PaymentPoint_.country), paymentTransfer.getDestinationCountry()),
                         cb.equal(root.get(PaymentPoint_.region), paymentTransfer.getDestinationRegion()),
                         cb.isMember(paymentTransfer.getTransferCurrency(), root.get(PaymentPoint_.currencies))))));
        } else {
            paymentSystemItems.forEach(paymentSystemItem -> paymentSystemItem.setEnabled(false));
        }
    }

    private void updateRegions() {
        regions = paymentTransfer.getDestinationCountry() != null ? regionRepository.findAll(((root, query, cb) -> {
            query.distinct(true);
            root.join(Region_.paymentPoints);
            return cb.and(cb.equal(root.get(Region_.country), paymentTransfer.getDestinationCountry()));
        }), new Sort(Region_.name.getName())) : null;
    }

    private void updateTransferCurrency() {
        transferCurrencies = paymentTransfer.getDestinationRegion() != null ? currencyRepository.findAll((root, query, cb) -> {
            query.distinct(true);
            Join<Currency, PaymentPoint> join = root.join(Currency_.paymentPoints);
            return cb.and(cb.equal(join.get(PaymentPoint_.country), paymentTransfer.getDestinationCountry()),
                          cb.equal(join.get(PaymentPoint_.region), paymentTransfer.getDestinationRegion()));
        }) : null;
    }

    public void selectDestinationCountry() {
        updateRegions();
        paymentTransfer.setDestinationRegion(null);
        paymentTransfer.setTransferCurrency(null);
        transferCurrencies = null;
        updatePaymentSystem();
    }

    public void selectDestinationRegion() {
        updateTransferCurrency();
        paymentTransfer.setTransferCurrency(null);
        updatePaymentSystem();
    }
}
