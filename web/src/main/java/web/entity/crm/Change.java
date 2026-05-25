/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.Department;
import web.entity.core.Task;
import web.entity.core.User;
import web.entity.log.ConnectionEvent;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "CHANGES", schema = "CRM")
public class Change implements Serializable {

    @Id
    @SequenceGenerator(name = "CHANGES_ID_SEQ", sequenceName = "CHANGES_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHANGES_ID_SEQ")
    @Column(name = "CHANGES_ID")
    private Long id;

    @Column(name = "CDATE")
    private Instant dateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private ChangeType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONS_ID")
    private Person person;

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

    @Column(name = "VERSION")
    private Integer version;

    @OneToMany(mappedBy = "change")
    private List<ChangeLog> changeLogList = new ArrayList<>();
}
