/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.entity.crm.Contact;
import web.entity.crm.Contact_;
import web.entity.crm.Person;
import web.repository.crm.ContactRepository;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public Contact findMainOrAnyOtherContact(Person person, String type) {
        return Optional.ofNullable(contactRepository.findOne((root, query, cb) -> cb
                .and(cb.equal(root.get(Contact_.person), person), cb.isTrue(root.get(Contact_.main)), cb.equal(root.get(Contact_.type), type))))
                       .orElse(contactRepository.findAny(
                               (root, query, cb) -> cb.and(cb.equal(root.get(Contact_.person), person), cb.equal(root.get(Contact_.type), type))));
    }
}
