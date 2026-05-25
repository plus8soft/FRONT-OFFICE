/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.log;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.AuthorizationResult;
import web.entity.core.Department;
import web.entity.core.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "CONNECTION_EVENTS", schema = "LOG")
public class ConnectionEvent implements Serializable {

    @Id
    @SequenceGenerator(name = "CONNECTION_EVENTS_ID_SEQ", sequenceName = "CONNECTION_EVENTS_ID_SEQ", schema = "LOG", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONNECTION_EVENTS_ID_SEQ")
    @Column(name = "CONNECTION_EVENTS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @Column(name = "USER_LOGIN")
    private String login;

    @Column(name = "EVENT_DATE")
    private Instant date;

    @Enumerated
    @Column(name = "EVENT_CODE")
    private AuthorizationResult authorizationResult;

    @Column(name = "USER_IP")
    private String ip;

    @Column(name = "USER_TIME_ZONE")
    private ZoneId zoneId;

    @Column(name = "USER_AGENT_STRING")
    private String userAgent;

    @Column(name = "LOGOFF_DATE")
    private Instant logoffDate;

    @Column(name = "LOGOFF_CORRECT")
    private boolean manualLogoff;
}
