/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import web.configuration.Settings;
import web.entity.core.User;
import web.entity.crm.Document;
import web.entity.crm.Person;
import web.repository.crm.DocumentRepository;
import web.service.back.PersonBackService;
import web.utils.Documents;

@Configurable
public class PassportValidator implements PersonValidator {

    @Autowired
    private PersonBackService personBackService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private Settings settings;

    @Override
    @Transactional
    public void validate(User user, Person person) throws PersonValidateException {
        if (!settings.isBackEnabled()) {
            return;
        }
        Document mainDoc = documentRepository.findMain(person);
        if (mainDoc.getType().equals(Documents.NATIONAL_PASSPORT_CODE) &&
            personBackService.checkPassport(user.getLogin(), mainDoc).getFmsInvalid()) {
            throw new PersonValidateException("Identity document is invalid");
        }
    }
}
