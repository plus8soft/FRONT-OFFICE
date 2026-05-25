/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.department.group.edit;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.core.Group_;
import web.repository.core.GroupRepository;
import web.view.Message;

@Getter
@Setter
@Component
public class DepartmentGroupNameValidator implements Validator, Message {

    @Autowired
    private GroupRepository groupRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue()) &&
            groupRepository.exists((root, query, cb) -> cb.and(cb.equal(root.get(Group_.name), value), cb.isFalse(root.get(Group_.areUser))))) {
            throw new ValidatorException(errorMessage("Department group name validation error.", "name is already in use"));
        }
    }
}
