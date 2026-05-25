/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.validator;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import web.configuration.Settings;
import web.entity.core.User;
import web.entity.crm.Person;
import web.service.back.PersonBackService;

@Configurable
public class TerroristValidator implements PersonValidator {

    @Autowired
    private PersonBackService personBackService;

    @Autowired
    private Settings settings;

    @Override
    @Transactional
    public void validate(User user, Person person) throws PersonValidateException {
        if (!settings.isBackEnabled()) {
            return;
        }
        if (personBackService.checkOnTerrorist(user.getLogin(), person.getExternalId()).getTerrorist()) {
            throw new PersonValidateException("Client performing the operation is on the terrorist list", Collections
                    .singletonList("Notify security service, financial monitoring department and interrupt the operation"));
        }
    }
}
