/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "USERS", schema = "CORE")
public class User implements Serializable {

    @Id
    @SequenceGenerator(name = "USERS_ID_SEQ", sequenceName = "USERS_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USERS_ID_SEQ")
    @Column(name = "USERS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @Column(name = "LOGIN")
    private String login;

    @Column(name = "PASS")
    private String password;

    @Column(name = "PASS_CHANGE_ON_LOGIN")
    private boolean requireChangePassword;

    @Column(name = "LASTNAME")
    private String lastname;

    @Column(name = "FIRSTNAME")
    private String firstname;

    @Column(name = "PATRONYMIC")
    private String patronymic;

    @Column(name = "ACCOUNT_EXP_DATE")
    private Instant accountExpirationDate;

    @Column(name = "LAST_LOGIN_EVENT")
    private Instant lastLoginEventDate;

    @Column(name = "USE_DEPARTMENT_TIME_ZONE")
    private boolean departmentZoneIdEnabled;

    @Column(name = "TIME_ZONE")
    private ZoneId zoneId;

    @Column(name = "JOB_POSITION")
    private Integer position;

    @Column(name = "JOB_POSITION_TXT")
    private String positionText;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "WORK_PHONE")
    private String workPhone;

    @Column(name = "MOB_PHONE")
    private String mobilePhone;

    @Column(name = "AUTH_TYPE")
    private String authorizationType;

    @Column(name = "STATUS")
    private String status;

    @ManyToMany
    @JoinTable(name = "USER_ROLES", schema = "CORE", joinColumns = @JoinColumn(name = "USERS_ID", referencedColumnName = "USERS_ID"),
               inverseJoinColumns = @JoinColumn(name = "ROLES_ID", referencedColumnName = "ROLES_ID"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "USER_RIGHTS", schema = "CORE", joinColumns = @JoinColumn(name = "USERS_ID", referencedColumnName = "USERS_ID"),
               inverseJoinColumns = @JoinColumn(name = "RIGHTS_ID", referencedColumnName = "RIGHTS_ID"))
    private Set<Right> rights = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "GROUP_USERS", schema = "CORE", joinColumns = @JoinColumn(name = "USERS_ID", referencedColumnName = "USERS_ID"),
               inverseJoinColumns = @JoinColumn(name = "GROUPS_ID", referencedColumnName = "GROUPS_ID"))
    private Set<Group> groups = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECURITY_PROFILES_ID")
    private SecurityProfile securityProfile;

    @ManyToMany
    @JoinTable(name = "USER_TASKS", schema = "CORE", joinColumns = @JoinColumn(name = "USERS_ID", referencedColumnName = "USERS_ID"),
               inverseJoinColumns = @JoinColumn(name = "TASKS_ID", referencedColumnName = "TASKS_ID"))
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private List<Certificate> certificates = new ArrayList<>();
}
