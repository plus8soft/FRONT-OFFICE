/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.log;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import web.entity.core.Department;
import web.entity.core.Task;
import web.entity.core.User;
import web.session.Menu;
import web.session.UserSession;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE")
@Table(name = "OPERATIONS", schema = "LOG")
public class Operation implements Serializable {

    @Id
    @SequenceGenerator(name = "OPERATIONS_ID_SEQ", sequenceName = "OPERATIONS_ID_SEQ", schema = "LOG", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OPERATIONS_ID_SEQ")
    @Column(name = "OPERATIONS_ID")
    private Long id;

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
    @JoinColumn(name = "CONNECTION_EVENTS_ID")
    private ConnectionEvent connectionEvent;

    @OneToOne(mappedBy = "operation", fetch = FetchType.LAZY)
    private PersonHistory personHistory;

    @Column(name = "EXT_OPERATIONS_ID")
    private Long externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", insertable = false, updatable = false)
    private OperationType type;

    @Column(name = "CODE")
    private OperationCode code;

    @Enumerated
    @Column(name = "STATUS")
    private OperationStatus status;

    @Column(name = "CDATE")
    private Instant instant;

    @Column(name = "LDATE")
    private LocalDateTime date;

    @PrePersist
    private void onPersist() {
        Menu menu = Menu.getInstance();
        UserSession userSession = menu.getUserSession();
        user = userSession.getUser();
        department = userSession.getUser().getDepartment();
        task = menu.getTask();
        connectionEvent = userSession.getConnectionEvent();
        instant = Instant.now();
    }
}
