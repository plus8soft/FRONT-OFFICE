/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.country;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.dict.Country_;
import web.repository.dict.CountryRepository;
import web.view.Message;

@Getter
@Setter
@Component
public class CountryValidator implements Validator, Message {

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue()) &&
            countryRepository.exists((root, query, cb) -> cb.equal(root.get(Country_.id), value))) {
            throw new ValidatorException(errorMessage("Parameter value cannot be used", "this ISO is already in use"));
        }
    }
}
