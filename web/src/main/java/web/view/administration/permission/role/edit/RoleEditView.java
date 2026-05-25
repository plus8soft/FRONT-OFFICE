/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.role.edit;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Group;
import web.entity.core.Right;
import web.entity.core.Role;
import web.entity.core.Task;
import web.repository.core.GroupRepository;
import web.repository.core.RightRepository;
import web.repository.core.RoleRepository;
import web.repository.core.TaskRepository;
import web.view.CheckboxTree;
import web.view.administration.model.UserFilter;
import web.view.administration.model.UserModel;

@Getter
@Setter
public class RoleEditView implements Serializable, CheckboxTree {

    private static final String TYPE_GROUP = "group";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RightRepository rightRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TaskRepository taskRepository;

    private Role role;

    private TreeNode rightTree;

    private TreeNode[] selectedRights;

    private TreeNode groupTree;

    private TreeNode[] selectedGroups;

    private TreeNode taskTree;

    private TreeNode[] selectedTasks;

    private UserFilter filter;

    private UserModel model;

    public void init(UserModel userModel) {
        filter = new UserFilter();
        if (role.getId() != null) {
            model = userModel;
            filter.setRole(role);
            model.setFilter(filter.clone());
        }
        buildGroupingTree(rightTree = new CheckboxTreeNode(), Right::getGroupName, rightRepository.findAll(), role.getRights(), !role.isSystem());
        buildTreeWithEmptyMessage(groupTree = new CheckboxTreeNode(), Group::getParent, Group::getPosition, groupRepository.findByAreUserIs(true),
                                  role.getGroups(), !role.isSystem(), false);
        buildGroupingTree(taskTree = new CheckboxTreeNode(), Task::getProject, Task::getParent, taskRepository.findFetchProject(), role.getTasks(),
                          !role.isSystem(), true);
    }

    public List<String> completeGroup(String group) {
        return roleRepository.findDistinctGroupNameByGroupName(group);
    }

    @Transactional
    public String save() {
        role.setSystem(false);
        Predicate<TreeNode> predicate = treeNode -> CheckboxTreeNode.DEFAULT_TYPE.equals(treeNode.getType());
        role.setRights(filterTreeArray(selectedRights, predicate));
        role.setTasks(filterTreeArray(selectedTasks, predicate.and((other) -> ((Task) other.getData()).getSystemName() != null)));
        role.setGroups(mapTree(selectedGroups));
        roleRepository.save(role);
        return "to-role";
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
