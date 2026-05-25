/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard.dictionary;

import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.core.Dictionary_;
import web.repository.core.DictionaryRepository;
import web.view.Message;

@Getter
@Setter
@Component
public class DictionaryValidator implements Validator, Message {

    private static final Pattern SYSTEM_NAME_PATTERN = Pattern.compile("[a-zA-Z_]+");

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null) {
            if (!SYSTEM_NAME_PATTERN.matcher((String) value).matches()) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "System name validation error.",
                                                              "only Latin letters and underscore are allowed"));
            }
            if (!value.equals(((UIInput) component).getValue()) &&
                dictionaryRepository.exists((root, query, cb) -> cb.equal(root.get(Dictionary_.id), value))) {
                throw new ValidatorException(errorMessage("System name validation error.", "system name is already in use"));
            }
        }
    }
}
