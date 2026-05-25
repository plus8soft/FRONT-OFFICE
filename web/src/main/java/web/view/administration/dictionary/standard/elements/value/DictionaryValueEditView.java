/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard.elements.value;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.dictionary.DictionaryValueCache;
import web.entity.core.DictionaryValue;

@Getter
@Setter
@Log4j2
public class DictionaryValueEditView implements Serializable {

    @Autowired
    private DictionaryValueCache dictionaryValueCache;

    private DictionaryValue dictionaryValue;

    public String save() {
        dictionaryValueCache.save(dictionaryValue);
        return "save";
    }
}
