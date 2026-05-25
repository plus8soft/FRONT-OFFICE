/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.department.group.edit;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import web.entity.core.Department;
import web.entity.core.Group;
import web.entity.core.Group_;
import web.repository.core.GroupRepository;
import web.service.administration.department.DepartmentService;
import web.view.administration.model.UserFilter;
import web.view.administration.model.UserModel;

@Getter
@Setter
@Log4j2
public class GroupEditView implements Serializable {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DepartmentService departmentService;

    private Group group;

    private List<Group> groups;

    private Department selectedDepartment;

    private List<Department> selectedDepartments;

    private Set<Department> departments;

    private UserModel model;

    private UserFilter filter;

    public void init(UserModel userModel) {
        filter = new UserFilter();
        if (group.getId() != null) {
            model = userModel;
            filter.setDepartmentGroup(group);
            model.setFilter(filter.clone());
            groups = groupRepository.findAll(Specifications.where((Specification<Group>) (root, query, cb) -> cb.isFalse(root.get(Group_.areUser)))
                                                           .and((root, query, cb) -> cb.notEqual(root, group)));
        }
        List<Department> departmentList = departmentService.getDepartmentFlatTree();
        departments = new TreeSet<>(Comparator.comparing(
                IntStream.range(0, departmentList.size()).boxed().collect(Collectors.toMap(departmentList::get, Function.identity()))::get));
        departments.addAll(departmentList.stream().filter(department -> !group.getDepartments().contains(department)).collect(Collectors.toList()));
    }

    public String save() {
        group.setAreUser(false);
        groupRepository.save(group);
        return "to-group";
    }

    public String formatZoneId(ZoneId zoneId) {
        return String.format("UTC%s", zoneId.getRules().getOffset(Instant.now()));
    }

    public void addDepartments() {
        group.getDepartments().addAll(selectedDepartments);
        departments.removeAll(selectedDepartments);
        selectedDepartments = null;
    }

    public void removeDepartments() {
        group.getDepartments().remove(selectedDepartment);
        departments.add(selectedDepartment);
        selectedDepartment = null;
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
        if (model != null) {
            model.setSelected(null);
            model.setFilter(filter.clone());
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:users")).reset();
        }
    }
}
