/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.settings.module.user.show;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.entity.core.Group_;
import web.entity.core.Role_;
import web.entity.core.SecurityProfile;
import web.entity.core.Task;
import web.entity.core.User;
import web.entity.core.User_;
import web.repository.core.GroupRepository;
import web.repository.core.RoleRepository;
import web.repository.core.SecurityProfileRepository;
import web.repository.core.UserRepository;
import web.service.administration.department.DepartmentService;

@Getter
@Setter
public class ModuleUserShowView implements Serializable {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SecurityProfileRepository securityProfileRepository;

    @Autowired
    private DepartmentService departmentService;

    private Task task;

    private User selected;

    private List<PermissionSourceItem> permissionSourceItems;

    private ModuleUserFilter filter;

    private ModuleUserModel model;

    private List<SecurityProfile> securityProfiles;

    private List<Department> departments;

    @Transactional
    public void init(ModuleUserModel moduleUserModel) {
        securityProfiles = securityProfileRepository.findAll();
        departments = departmentService.getDepartmentFlatTree();
        filter = new ModuleUserFilter(task);
        model = moduleUserModel;
        model.setFilter(filter.clone());
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    private void updateFilter() {
        model.setFilter(filter.clone());
        model.reset();
        selected = null;
        permissionSourceItems = null;
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:users")).reset();
    }

    public void updatePermissionSource() {
        permissionSourceItems = new ArrayList<>();
        groupRepository.findAll((root, query, cb) -> {
            query.distinct(true);
            return cb.and(cb.isTrue(root.get(Group_.areUser)), cb.isMember(selected, root.get(Group_.users)),
                          cb.isMember(task, root.join(Group_.roles).get(Role_.tasks)));
        }).forEach(group -> roleRepository
                .findAll((root, query, cb) -> cb.and(cb.isMember(group, root.get(Role_.groups)), cb.isMember(task, root.get(Role_.tasks))))
                .forEach(role -> permissionSourceItems.add(new PermissionSourceItem(group.getName(), role.getName(), task.getName()))));
        roleRepository.findAll((root, query, cb) -> cb.and(cb.isMember(task, root.get(Role_.tasks)), cb.isMember(selected, root.get(Role_.users))))
                      .forEach(role -> permissionSourceItems.add(new PermissionSourceItem(null, role.getName(), task.getName())));
        userRepository.findAll((root, query, cb) -> cb.and(cb.isMember(task, root.get(User_.tasks)), cb.equal(root, selected)))
                      .forEach(user -> permissionSourceItems.add(new PermissionSourceItem(null, null, task.getName())));
    }
}
