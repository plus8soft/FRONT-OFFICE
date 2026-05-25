/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.time.LocalTime;
import java.time.ZoneId;
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
import web.entity.ce.DepartmentCurrency;
import web.entity.dict.AccountLink;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "DEPARTMENTS", schema = "CORE")
public class Department extends BaseAddress {

    @Id
    @SequenceGenerator(name = "DEPARTMENTS_ID_SEQ", sequenceName = "DEPARTMENTS_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPARTMENTS_ID_SEQ")
    @Column(name = "DEPARTMENTS_ID")
    private Long id;

    @Column(name = "EXT_DEPARTMENTS_ID")
    private Long externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_DEPARTMENT")
    private Department parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("position")
    private List<Department> childs = new ArrayList<>();

    @Column(name = "POSITION")
    private Integer position;

    @Column(name = "TYPE")
    private Integer type;

    @Column(name = "STATUS")
    private boolean enabled;

    @Column(name = "CODE")
    private String unitCode;

    @Column(name = "SHORT_NAME")
    private String name;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "WORK_PHONE")
    private String workPhone;

    @Column(name = "MOB_PHONE")
    private String mobilePhone;

    @Column(name = "START_OPERATION_DAY")
    private LocalTime startOperationDay;

    @Column(name = "END_OPERATION_DAY")
    private LocalTime endOperationDay;

    @Column(name = "NIGHTLY_CASH")
    private boolean nightCash;

    @Column(name = "TIME_ZONE")
    private ZoneId zoneId;

    @OneToMany(mappedBy = "department")
    private List<DepartmentCurrency> departmentCurrencies = new ArrayList<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.REMOVE)
    private List<AccountLink> accountLinks = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "GROUP_DEPARTMENTS", schema = "CORE",
               joinColumns = @JoinColumn(name = "DEPARTMENTS_ID", referencedColumnName = "DEPARTMENTS_ID"),
               inverseJoinColumns = @JoinColumn(name = "GROUPS_ID", referencedColumnName = "GROUPS_ID"))
    private Set<Group> groups = new HashSet<>();

    @OneToMany(mappedBy = "department")
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    private List<DepartmentPaymentSystem> departmentPaymentSystems = new ArrayList<>();
}
