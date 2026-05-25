/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard.dictionary;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Dictionary;
import web.repository.core.DictionaryRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class DictionaryEditView implements Message, Serializable {

    @Autowired
    private DictionaryRepository dictionaryRepository;

    private Dictionary dictionary;

    @Transactional
    public String save() {
        dictionary.setUpdateDate(Instant.now());
        dictionaryRepository.save(dictionary);
        return "save";
    }

    public List<String> completeGroup(String group) {
        return dictionaryRepository.findDictionaryByGroup(group);
    }
}
