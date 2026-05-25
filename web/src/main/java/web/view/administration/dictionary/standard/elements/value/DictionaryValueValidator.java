/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard.elements.value;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.core.Dictionary;
import web.entity.core.DictionaryValue_;
import web.repository.core.DictionaryValueRepository;
import web.view.Message;

@Getter
@Setter
@Component
public class DictionaryValueValidator implements Validator, Message {

    @Autowired
    private DictionaryValueRepository dictionaryValueRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue()) && dictionaryValueRepository.exists((root, query, cb) -> cb
                .and(cb.equal(root.get(DictionaryValue_.code), (String) value),
                     cb.equal(root.get(DictionaryValue_.dictionary), (Dictionary) component.getAttributes().get("dictionary"))))) {
            throw new ValidatorException(errorMessage("Code validation error.", "code is already in use"));
        }
    }
}
