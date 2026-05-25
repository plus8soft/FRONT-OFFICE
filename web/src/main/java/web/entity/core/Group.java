/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
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
import javax.persistence.OrderBy;
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
@Table(name = "GROUPS", schema = "CORE")
public class Group implements Serializable {

    @Id
    @SequenceGenerator(name = "GROUPS_ID_SEQ", sequenceName = "GROUPS_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GROUPS_ID_SEQ")
    @Column(name = "GROUPS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_GROUP")
    private Group parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("position")
    private List<Group> childs = new ArrayList<>();

    @Column(name = "POSITION")
    private Integer position;

    @Column(name = "TYPE")
    private boolean areUser;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToMany
    @JoinTable(name = "GROUP_DEPARTMENTS", schema = "CORE", joinColumns = @JoinColumn(name = "GROUPS_ID", referencedColumnName = "GROUPS_ID"),
               inverseJoinColumns = @JoinColumn(name = "DEPARTMENTS_ID", referencedColumnName = "DEPARTMENTS_ID"))
    private Set<Department> departments = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "GROUP_USERS", schema = "CORE", joinColumns = @JoinColumn(name = "GROUPS_ID", referencedColumnName = "GROUPS_ID"),
               inverseJoinColumns = @JoinColumn(name = "USERS_ID", referencedColumnName = "USERS_ID"))
    private Set<User> users = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "GROUP_ROLES", schema = "CORE", joinColumns = @JoinColumn(name = "GROUPS_ID", referencedColumnName = "GROUPS_ID"),
               inverseJoinColumns = @JoinColumn(name = "ROLES_ID", referencedColumnName = "ROLES_ID"))
    private Set<Role> roles = new HashSet<>();
}
