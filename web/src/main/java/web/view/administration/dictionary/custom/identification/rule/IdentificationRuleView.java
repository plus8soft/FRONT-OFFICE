/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.identification.rule;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.IdentificationRule;
import web.entity.dict.IdentificationRule_;
import web.repository.dict.IdentificationRuleRepository;

@Getter
@Setter
@Log4j2
public class IdentificationRuleView implements Serializable {

    @Autowired
    private IdentificationRuleRepository identificationRuleRepository;

    private DictionaryParameter dictionary;

    private IdentificationRule selected;

    private List<IdentificationRule> identificationRules;

    public void init(DictionaryParameter dictionary) {
        this.dictionary = dictionary;
        identificationRules = identificationRuleRepository.findAll((root, query, cb) -> {
            root.fetch(IdentificationRule_.task);
            return null;
        });
    }
}
