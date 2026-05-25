/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.validator;

import java.time.LocalDate;
import java.time.ZoneId;
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
public class MinCurrentLocalDateValidator implements Validator, Message {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue()) &&
            ((LocalDate) value).isBefore(LocalDate.now((ZoneId) component.getAttributes().get("zoneId")))) {
            throw new ValidatorException(errorMessage("Date input error", "date must be greater than or equal to current date"));
        }
    }
}
