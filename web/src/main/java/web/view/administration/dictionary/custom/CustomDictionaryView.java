/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.DictionaryParameter_;
import web.repository.dict.DictionaryParameterRepository;
import web.service.dict.scheduling.DictionaryScheduler;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class CustomDictionaryView implements Message, Serializable {

    @Autowired
    private DictionaryParameterRepository dictionaryParameterRepository;

    @Autowired
    private DictionaryScheduler scheduler;

    private List<DictionaryParameter> dictionaryParameters;

    private DictionaryParameter selected;

    public void init() {
        dictionaryParameters =
                dictionaryParameterRepository.findAll(new Sort(DictionaryParameter_.group.getName(), DictionaryParameter_.name.getName()));
    }

    public void update() {
        if (selected == null) {
            return;
        }
        scheduler.submit(selected);
        init();
    }
}
