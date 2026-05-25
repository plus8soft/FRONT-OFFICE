/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.woa.payment;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.dictionary.AddressTypeDictionary;
import web.entity.crm.Address;
import web.entity.dict.PurposeMacros;
import web.repository.crm.AddressRepository;
import web.repository.crm.PersonRepository;
import web.utils.Addresses;
import web.utils.Utils;

@Getter
@Setter
@Log4j2
public class StepThreeView implements Serializable {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressTypeDictionary addressTypeDictionary;

    @Autowired
    private Utils utils;

    private WoaPayment payment;

    private Address personAddress;

    private Address livingAddress;

    private Address currentAddress;

    public void init(WoaPayment payment, Long personId) {
        this.payment = payment;
        if (personId != null) {
            this.payment.setPerson(personRepository.findOne(personId));
            personAddress = addressRepository.findByPersonAndType(payment.getPerson(), Addresses.STAYING_TYPE);
            if (personAddress == null) {
                personAddress = addressRepository.findByPersonAndType(payment.getPerson(), Addresses.RESIDENTIAL_TYPE);
            }
        }
        if (payment.getCounteragent().getPurposeTemplate() != null) {
            String purpose = payment.getCounteragent().getPurposeTemplate()
                                    .replace("#" + PurposeMacros.PARTNER_NAME.name() + "#", payment.getCounteragent().getName())
                                    .replace("#" + PurposeMacros.OPERATION.name() + "#", payment.getPayAction().getName())
                                    .replace("#" + PurposeMacros.VAT.name() + "#", payment.getPayAction().getVat() + "%")
                                    .replace("#" + PurposeMacros.CLIENT_FIO.name() + "#", payment.getPerson() != null ? utils.getStrings().joinFio(
                                            payment.getPerson().getLastname(), payment.getPerson().getFirstname(),
                                            payment.getPerson().getPatronymic()) : "")
                                    .replace("#" + PurposeMacros.AMOUNT.name() + "#", payment.getTotal().toString());
            payment.setPurpose(purpose);
        }
    }

    public String goBack() {
        return payment.getPerson() != null ? "back-search" : "back";
    }
}
