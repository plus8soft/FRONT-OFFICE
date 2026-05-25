/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payaction;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.PayAction;
import web.repository.dict.PayActionRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class EditView implements Message, Serializable {

    @Autowired
    private PayActionRepository payActionRepository;

    private DictionaryParameter dictionary;

    private PayAction payAction;

    public String save() {
        try {
            payActionRepository.save(payAction);
            return "save";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            return null;
        }
    }
}
