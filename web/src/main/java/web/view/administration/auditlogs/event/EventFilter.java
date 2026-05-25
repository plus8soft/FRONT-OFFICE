/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import web.entity.core.Department;
import web.entity.core.EventCode;
import web.entity.core.EventStatus;
import web.entity.core.Project;
import web.entity.core.Task;
import web.entity.core.User;

@Data
public class EventFilter implements Serializable, Cloneable {

    private List<EventStatus> types = new ArrayList<>();

    private List<EventCode> codes = new ArrayList<>();

    private Instant eventDateWith;

    private Instant eventDate;

    private Long eventId;

    private List<User> users = new ArrayList<>();

    private List<Department> departments = new ArrayList<>();

    private List<Project> projects = new ArrayList<>();

    private List<Task> tasks = new ArrayList<>();

    @Override
    public EventFilter clone() {
        try {
            return (EventFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
