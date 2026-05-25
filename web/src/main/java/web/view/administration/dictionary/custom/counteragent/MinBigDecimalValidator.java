/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

import java.math.BigDecimal;
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
public class MinBigDecimalValidator implements Validator, Message {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        UIInput maxComponent = (UIInput) component.getAttributes().get("max");
        String maxText = (String) maxComponent.getSubmittedValue();
        BigDecimal max = maxText.isEmpty() ? null : new BigDecimal(maxText);
        if (value != null && max != null && ((BigDecimal) value).compareTo(max) >= 0) {
            throw new ValidatorException(errorMessage("Input error", "minimum amount must be less than maximum"));
        }
    }
}
