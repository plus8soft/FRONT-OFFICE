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
@Table(name = "RIGHTS", schema = "CORE")
public class Right implements Serializable {

    @Id
    @SequenceGenerator(name = "RIGHTS_ID_SEQ", sequenceName = "RIGHTS_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RIGHTS_ID_SEQ")
    @Column(name = "RIGHTS_ID")
    private Long id;

    @Column(name = "GROUP_NAME")
    private String groupName;

    @Column(name = "SYSTEM_NAME")
    private String systemName;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToMany
    @JoinTable(name = "ROLE_RIGHTS", schema = "CORE", joinColumns = @JoinColumn(name = "RIGHTS_ID", referencedColumnName = "RIGHTS_ID"),
               inverseJoinColumns = @JoinColumn(name = "ROLES_ID", referencedColumnName = "ROLES_ID"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "USER_RIGHTS", schema = "CORE", joinColumns = @JoinColumn(name = "RIGHTS_ID", referencedColumnName = "RIGHTS_ID"),
               inverseJoinColumns = @JoinColumn(name = "USERS_ID", referencedColumnName = "USERS_ID"))
    private Set<User> users = new HashSet<>();
}
