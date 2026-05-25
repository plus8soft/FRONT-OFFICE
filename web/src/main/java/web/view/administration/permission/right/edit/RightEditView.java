/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.right.edit;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Right;
import web.entity.core.Role;
import web.repository.core.RightRepository;
import web.repository.core.RoleRepository;
import web.view.CheckboxTree;
import web.view.administration.model.UserFilter;
import web.view.administration.model.UserModel;

@Getter
@Setter
public class RightEditView implements Serializable, CheckboxTree {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RightRepository rightRepository;

    private Right right;

    private TreeNode roleTree;

    private TreeNode[] selectedRoles;

    private UserFilter filter;

    private UserModel model;

    public void init(UserModel userModel) {
        filter = new UserFilter();
        model = userModel;
        filter.setRight(right);
        model.setFilter(filter.clone());
        buildGroupingTree(roleTree = new CheckboxTreeNode(), Role::getGroupName, roleRepository.findAll(), right.getRoles(), true);
    }

    @Transactional
    public String save() {
        right.setRoles(filterTreeArray(selectedRoles, treeNode -> CheckboxTreeNode.DEFAULT_TYPE.equals(treeNode.getType())));
        rightRepository.save(right);
        return "to-right";
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
