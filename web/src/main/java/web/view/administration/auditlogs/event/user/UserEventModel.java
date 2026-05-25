/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event.user;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.log.UserEvent;
import web.repository.log.UserEventRepository;
import web.view.administration.auditlogs.event.AbstractEventModel;

public class UserEventModel extends AbstractEventModel<UserEvent> implements Serializable {

    @Autowired
    private UserEventRepository userEventRepository;

    @Override
    public UserEventRepository getEventRepository() {
        return userEventRepository;
    }
}
