/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.department.management.edit;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.core.Department_;
import web.repository.core.DepartmentRepository;
import web.view.Message;

@Getter
@Setter
@Component
public class DepartmentExternalIdValidator implements Validator, Message {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue()) &&
            departmentRepository.exists((root, query, cb) -> cb.equal(root.get(Department_.externalId), value))) {
            throw new ValidatorException(errorMessage("ID validation error.", "ID is already in use"));
        }
    }
}
