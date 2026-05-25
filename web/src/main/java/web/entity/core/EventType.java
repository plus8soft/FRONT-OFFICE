/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import lombok.Getter;
import web.entity.log.AbstractEvent;
import web.entity.log.SystemEvent;
import web.entity.log.UserEvent;

public enum EventType {
    SYSTEM(SystemEvent.class),
    USER(UserEvent.class);

    @Getter
    private Class<? extends AbstractEvent> eventClass;

    EventType(Class<? extends AbstractEvent> eventClass) {
        this.eventClass = eventClass;
    }
}
