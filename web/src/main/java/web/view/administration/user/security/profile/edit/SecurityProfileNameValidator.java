/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.security.profile.edit;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.core.SecurityProfile_;
import web.repository.core.SecurityProfileRepository;
import web.view.Message;

@Getter
@Setter
@Component
public class SecurityProfileNameValidator implements Validator, Message {

    @Autowired
    private SecurityProfileRepository securityProfileRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue()) &&
            securityProfileRepository.exists((root, query, cb) -> cb.equal(root.get(SecurityProfile_.name), value))) {
            throw new ValidatorException(errorMessage("Security profile name validation error.", "name is already in use"));
        }
    }
}
