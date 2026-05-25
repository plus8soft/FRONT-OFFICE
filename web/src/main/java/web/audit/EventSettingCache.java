/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.audit;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import web.entity.core.EventCode;
import web.entity.core.EventSetting;
import web.entity.core.EventSetting_;
import web.entity.core.EventType;
import web.repository.core.EventSettingRepository;

@Component
public class EventSettingCache {

    @Autowired
    private EventSettingRepository eventSettingRepository;

    @Cacheable("event-enabled")
    public boolean isEnabled(EventCode code) {
        return eventSettingRepository
                .exists((root, query, cb) -> cb.and(cb.equal(root.get(EventSetting_.code), code), cb.isTrue(root.get(EventSetting_.enabled))));
    }

    public List<EventSetting> save(Collection<EventSetting> eventSettings) {
        return eventSettingRepository.save(eventSettings.stream().peek(this::updateEnabled).peek(this::updateType).collect(Collectors.toList()));
    }

    @CachePut(cacheNames = "event-enabled", key = "#eventSetting.code")
    public boolean updateEnabled(EventSetting eventSetting) {
        return eventSetting.isEnabled();
    }

    @Cacheable("event-type")
    public EventType getType(EventCode code) {
        return eventSettingRepository.findOne((root, query, cb) -> cb.equal(root.get(EventSetting_.code), code)).getType();
    }

    @CachePut(cacheNames = "event-type", key = "#eventSetting.code")
    public EventType updateType(EventSetting eventSetting) {
        return eventSetting.getType();
    }
}
