/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.role.edit;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.core.Role_;
import web.repository.core.RoleRepository;
import web.view.Message;

@Getter
@Setter
@Component
public class RoleNameValidator implements Validator, Message {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue()) &&
            roleRepository.exists((root, query, cb) -> cb.equal(root.get(Role_.name), value))) {
            throw new ValidatorException(errorMessage("Role name validation error.", "name is already in use"));
        }
    }
}
