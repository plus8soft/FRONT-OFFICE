/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.rule;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.ce.Rule;
import web.repository.ce.RuleRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class RuleEditView implements Message, Serializable {

    @Autowired
    private RuleRepository ruleRepository;

    private Rule rule;

    public String save() {
        String action = null;
        try {
            if (rule.getMax() != null && rule.getMin().compareTo(rule.getMax()) == 1) {
                addErrorMessage("Maximum amount cannot be less than minimum amount.");
            } else {
                if (rule.getId() == null) {
                    rule.setPosition(ruleRepository.findLastPosition().map(position -> position + 1).orElse(0));
                }
                ruleRepository.save(rule);
                addInfoMessage("Data saved successfully.");
                action = "save";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            action = null;
        }
        return action;
    }
}
