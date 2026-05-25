/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.service.dict.scheduling.DictionaryScheduler;
import web.session.UserSession;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class CustomDictionaryEdit implements Serializable, Message {

    @Autowired
    private UserSession userSession;

    @Autowired
    private DictionaryScheduler dictionaryScheduler;

    private DictionaryParameter dictionaryParameter;

    private boolean changeParameters;

    public void init(DictionaryParameter dictionaryParameter) {
        this.dictionaryParameter = dictionaryParameter;
    }

    public String save() {
        if (dictionaryParameter == null) {
            addErrorMessage("No dictionary selected.");
            return null;
        }
        try {
            dictionaryScheduler.updateSchedule(dictionaryParameter);
            addInfoMessage("Data saved successfully.");
            return "cancel";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            return null;
        }
    }
}
