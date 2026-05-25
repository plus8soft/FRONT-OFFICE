/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.validator;

import java.util.regex.Pattern;
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
public class EmailValidator implements Validator, Message {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !EMAIL_PATTERN.matcher((String) value).matches()) {
            throw new ValidatorException(errorMessage("Email validation error", "format is not valid"));
        }
    }
}
