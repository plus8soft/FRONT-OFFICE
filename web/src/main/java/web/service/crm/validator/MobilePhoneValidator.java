/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.validator;

import java.util.Collections;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import web.entity.core.User;
import web.entity.crm.Contact;
import web.entity.crm.Person;
import web.service.crm.ContactService;
import web.utils.Contacts;

@Configurable
public class MobilePhoneValidator implements PersonValidator {

    private static final Pattern MOBILE_PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$");

    @Autowired
    private ContactService contactService;

    @Override
    public void validate(User user, Person person) throws PersonValidateException {
        Contact mobilePhone = contactService.findMainOrAnyOtherContact(person, Contacts.MOBILE_PHONE_TYPE);
        if (mobilePhone == null || !MOBILE_PHONE_PATTERN.matcher(mobilePhone.getData()).matches()) {
            throw new PersonValidateException("Client mobile phone number is missing or incorrect", Collections
                    .singletonList("Update client data regarding mobile phone number"));
        }
    }
}
