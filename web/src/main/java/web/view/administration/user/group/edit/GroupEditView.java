/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.group.edit;

import java.io.Serializable;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.entity.core.Group;
import web.entity.core.Group_;
import web.entity.core.Role;
import web.repository.core.GroupRepository;
import web.repository.core.RoleRepository;
import web.service.administration.department.DepartmentService;
import web.view.CheckboxTree;
import web.view.administration.model.UserFilter;
import web.view.administration.model.UserModel;

@Getter
@Setter
public class GroupEditView implements Serializable, CheckboxTree {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DepartmentService departmentService;

    private Group group;

    private List<Department> departments;

    private UserFilter filter;

    private UserModel model;

    private TreeNode roleTree;

    private TreeNode[] selectedRoles;

    private List<Group> groups;

    public void init(UserModel userModel) {
        filter = new UserFilter();
        if (group.getId() != null) {
            model = userModel;
            filter.setUserGroup(group);
            model.setFilter(filter.clone());
            groups = groupRepository.findAll(Specifications.where((Specification<Group>) (root, query, cb) -> cb.isTrue(root.get(Group_.areUser)))
                                                           .and((root, query, cb) -> cb.notEqual(root, group)));
        }
        departments = departmentService.getDepartmentFlatTree();
        buildGroupingTree(roleTree = new CheckboxTreeNode(), Role::getGroupName, roleRepository.findAll(), group.getRoles(), true);
    }

    @Transactional
    public String save() {
        group.setAreUser(true);
        group.setRoles(filterTreeArray(selectedRoles, treeNode -> CheckboxTreeNode.DEFAULT_TYPE.equals(treeNode.getType())));
        groupRepository.save(group);
        return "to-group";
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    public void updateFilter() {
        if (model != null) {
            model.setSelected(null);
            model.setFilter(filter.clone());
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("content:users")).reset();
        }
    }
}
