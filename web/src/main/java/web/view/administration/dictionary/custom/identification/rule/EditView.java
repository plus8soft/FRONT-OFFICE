/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.identification.rule;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.IdentificationRule;
import web.repository.dict.IdentificationRuleRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class EditView implements Message, Serializable {

    @Autowired
    private IdentificationRuleRepository identificationRuleRepository;

    private DictionaryParameter dictionary;

    private IdentificationRule rule;

    public String save() {
        try {
            identificationRuleRepository.save(rule);
            addInfoMessage("Data saved successfully.");
            return "save";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            return null;
        }
    }
}
