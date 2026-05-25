/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.audit.management.edit;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.audit.EventSettingCache;
import web.entity.core.EventSetting;
import web.entity.core.EventSetting_;
import web.entity.core.Task;
import web.repository.core.EventSettingRepository;

@Getter
@Setter
@Log4j2
public class EditView implements Serializable {

    @Autowired
    private EventSettingRepository eventSettingRepository;

    @Autowired
    private EventSettingCache eventSettingCache;

    private List<EventSetting> eventSettings;

    private Task task;

    private boolean changeData;

    public void init() {
        eventSettings = eventSettingRepository.findAll(((root, query, cb) -> cb.equal(root.get(EventSetting_.task), task)));
    }

    public String save() {
        eventSettingCache.save(eventSettings);
        return "event-setting";
    }
}
