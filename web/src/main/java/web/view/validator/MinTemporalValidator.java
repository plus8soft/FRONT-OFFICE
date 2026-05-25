/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.calendar.Calendar;
import org.springframework.stereotype.Component;
import web.view.Message;

@Getter
@Setter
@Component
public class MinTemporalValidator implements Validator, Message {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Object minDate = ((Calendar) component).getMindate();
        if (value != null && minDate != null && ((Comparable<Object>) minDate).compareTo(value) > 0) {
            throw new ValidatorException(errorMessage("Date validation error.", String.join(" ", "date earlier than", minDate.toString(), "is not allowed")));
        }
    }
}
