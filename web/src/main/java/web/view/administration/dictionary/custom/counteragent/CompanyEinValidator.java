/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import web.view.Message;

@Getter
@Setter
@Component
public class CompanyEinValidator implements Validator, Message {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue())) {
            String val = ((String) value).replaceAll("[^0-9]", "");
            // EIN format: 9 digits (XX-XXXXXXX or XXXXXXXXX)
            if (val.length() != 9 || !val.matches("\\d{9}")) {
                throw new ValidatorException(errorMessage("EIN Validation Error", "EIN must be 9 digits"));
            }
        }
    }
}
