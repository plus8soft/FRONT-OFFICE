/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.department.management.edit;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.repository.core.DepartmentRepository;

@Getter
@Setter
@Component
public class DepartmentStatusValidator implements Validator {

    @Autowired
    private DepartmentRepository departmentRepository;

    private boolean hasActiveChilds(Long parentId) {
        return (departmentRepository.countActiveChildren(parentId) > 0);
    }

    private boolean hasNotActiveParents(Long departmentId) {
        return (departmentRepository.countNotActiveParents(departmentId) > 0);
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue())) {
            if (!((boolean) value) && hasActiveChilds((Long) component.getAttributes().get("departmentId"))) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot change active status.",
                                                              "department has active child elements"));
            }
            if (((boolean) value) && hasNotActiveParents((Long) component.getAttributes().get("departmentId"))) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot change active status.",
                                                              "department has inactive parent elements"));
            }
        }
    }
}
