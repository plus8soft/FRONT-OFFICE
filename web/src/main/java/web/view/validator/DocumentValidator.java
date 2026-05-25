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
import web.utils.Documents;
import web.view.Message;

@Getter
@Setter
@Component
public class DocumentValidator implements Validator, Message {

    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("[^0-9a-zA-Z-]");

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !Documents.NATIONAL_PASSPORT_CODE.equals(component.getAttributes().get("type")) &&
            DOCUMENT_PATTERN.matcher((String) value).find()) {
            throw new ValidatorException(errorMessage("Document validation error", "contains invalid characters"));
        }
    }
}
