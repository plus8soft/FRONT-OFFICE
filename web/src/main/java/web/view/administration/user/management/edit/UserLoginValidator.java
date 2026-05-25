/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.management.edit;

import java.util.regex.Pattern;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.core.User_;
import web.repository.core.UserRepository;
import web.view.Message;

@Getter
@Setter
@Component
public class UserLoginValidator implements Validator, Message {

    private static final Pattern LOGIN_PATTERN = Pattern.compile("[a-zA-Z0-9_~,.!;%:?*()\\[\\]{}`+=@#$\\^&/|\\\\<>'\"-]+");

    @Autowired
    private UserRepository userRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null) {
            if (!LOGIN_PATTERN.matcher((String) value).matches()) {
                throw new ValidatorException(
                        errorMessage("Login validation error.", "only Latin letters, numbers and special characters are allowed"));
            }
            if (!value.equals(((UIInput) component).getValue()) &&
                userRepository.exists((root, query, cb) -> cb.equal(root.get(User_.login), value))) {
                throw new ValidatorException(errorMessage("Login validation error.", "login is already in use"));
            }
        }
    }
}
