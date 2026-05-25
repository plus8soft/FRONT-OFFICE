/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.log;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import web.entity.core.Department;
import web.entity.core.EventCode;
import web.entity.core.EventStatus;
import web.entity.core.Project;
import web.entity.core.Task;
import web.entity.core.User;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEvent implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASKS_ID")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECTS_ID")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONNECTION_EVENTS_ID")
    private ConnectionEvent connectionEvent;

    @Column(name = "EVENT_DATE")
    private Instant date;

    @Column(name = "EVENT_GROUP")
    private UUID group;

    @Column(name = "EVENT_CODE")
    private EventCode code;

    @Enumerated
    @Column(name = "EVENT_TYPE")
    private EventStatus status;

    @Column(name = "EVENT_DESCRIPTION")
    private String description;

    public abstract Long getId();

    public abstract void setId(Long id);
}
