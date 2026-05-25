/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.validator;

import java.time.LocalDate;
import java.time.ZoneId;
import javax.faces.component.UIComponent;
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
public class MaxCurrentLocalDateValidator implements Validator, Message {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        ZoneId zoneId = (ZoneId) component.getAttributes().get("zoneId");
        if (value != null && ((LocalDate) value).isAfter(zoneId == null ? LocalDate.now() : LocalDate.now(zoneId))) {
            throw new ValidatorException(errorMessage("Date validation error.", "future date is not allowed"));
        }
    }
}
