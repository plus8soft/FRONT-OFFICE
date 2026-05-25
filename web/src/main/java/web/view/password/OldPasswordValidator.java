/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.password;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import web.view.Message;

@Component
public class OldPasswordValidator implements Validator, Message {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !passwordEncoder.matches((CharSequence) value, (String) component.getAttributes().get("password"))) {
            throw new ValidatorException(errorMessage("Old password validation error.", "passwords do not match"));
        }
    }
}
