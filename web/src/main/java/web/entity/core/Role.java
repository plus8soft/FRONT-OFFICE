/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name = "ROLES", schema = "CORE")
public class Role implements Serializable {

    @Id
    @SequenceGenerator(name = "ROLES_ID_SEQ", sequenceName = "ROLES_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ROLES_ID_SEQ")
    @Column(name = "ROLES_ID")
    private Long id;

    @Column(name = "IS_SYSTEM")
    private boolean system;

    @Column(name = "GROUP_NAME")
    private String groupName;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToMany
    @JoinTable(name = "ROLE_RIGHTS", schema = "CORE", joinColumns = @JoinColumn(name = "ROLES_ID", referencedColumnName = "ROLES_ID"),
               inverseJoinColumns = @JoinColumn(name = "RIGHTS_ID", referencedColumnName = "RIGHTS_ID"))
    private Set<Right> rights = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "ROLE_TASKS", schema = "CORE", joinColumns = @JoinColumn(name = "ROLES_ID", referencedColumnName = "ROLES_ID"),
               inverseJoinColumns = @JoinColumn(name = "TASKS_ID", referencedColumnName = "TASKS_ID"))
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "GROUP_ROLES", schema = "CORE", joinColumns = @JoinColumn(name = "ROLES_ID", referencedColumnName = "ROLES_ID"),
               inverseJoinColumns = @JoinColumn(name = "GROUPS_ID", referencedColumnName = "GROUPS_ID"))
    private Set<Group> groups = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "USER_ROLES", schema = "CORE", joinColumns = @JoinColumn(name = "ROLES_ID", referencedColumnName = "ROLES_ID"),
               inverseJoinColumns = @JoinColumn(name = "USERS_ID", referencedColumnName = "USERS_ID"))
    private Set<User> users = new HashSet<>();
}
