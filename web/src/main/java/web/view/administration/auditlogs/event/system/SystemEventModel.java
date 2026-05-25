/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event.system;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.log.SystemEvent;
import web.repository.log.SystemEventRepository;
import web.view.administration.auditlogs.event.AbstractEventModel;

public class SystemEventModel extends AbstractEventModel<SystemEvent> implements Serializable {

    @Autowired
    private SystemEventRepository systemEventRepository;

    @Override
    public SystemEventRepository getEventRepository() {
        return systemEventRepository;
    }
}
