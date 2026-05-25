/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
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
@Table(name = "TASKS", schema = "CORE")
public class Task implements Serializable {

    @Id
    @SequenceGenerator(name = "TASKS_ID_SEQ", sequenceName = "TASKS_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TASKS_ID_SEQ")
    @Column(name = "TASKS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TASK")
    private Task parent;

    @OneToMany(mappedBy = "parent")
    private List<Task> childs = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECTS_ID")
    private Project project;

    @Column(name = "SYSTEM_NAME")
    private String systemName;

    @Column(name = "NAME")
    private String name;

    @ManyToMany
    @JoinTable(name = "ROLE_TASKS", schema = "CORE", joinColumns = @JoinColumn(name = "TASKS_ID", referencedColumnName = "TASKS_ID"),
               inverseJoinColumns = @JoinColumn(name = "ROLES_ID", referencedColumnName = "ROLES_ID"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "USER_TASKS", schema = "CORE", joinColumns = @JoinColumn(name = "TASKS_ID", referencedColumnName = "TASKS_ID"),
               inverseJoinColumns = @JoinColumn(name = "USERS_ID", referencedColumnName = "USERS_ID"))
    private Set<User> users = new HashSet<>();
}
