/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.validator;

import web.entity.core.User;
import web.entity.crm.Person;

public interface PersonValidator {

    void validate(User user, Person person) throws PersonValidateException;
}
