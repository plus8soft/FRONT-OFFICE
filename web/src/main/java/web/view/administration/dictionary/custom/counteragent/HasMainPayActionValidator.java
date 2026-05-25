/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

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
public class HasMainPayActionValidator implements Validator, Message {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        boolean has = (boolean) component.getAttributes().get("hasMain");
        if (value != null && (boolean) value && has) {
            throw new ValidatorException(errorMessage("Input error", "only one main payment action is allowed"));
        }
    }
}
