/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

import java.time.LocalDate;
import java.util.Set;
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
public class CommissionDateExistsValidator implements Validator, Message {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && ((Set<LocalDate>) component.getAttributes().get("dates")).contains(value)) {
            throw new ValidatorException(errorMessage("Date input error", "commissions are already assigned for this date"));
        }
    }
}
