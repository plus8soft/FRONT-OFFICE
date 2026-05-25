/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.log.operation;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.entity.core.Department_;
import web.entity.core.Task;
import web.entity.core.User;
import web.entity.core.User_;
import web.repository.core.DepartmentRepository;
import web.repository.core.TaskRepository;
import web.repository.core.UserRepository;
import web.service.administration.department.DepartmentService;
import web.session.UserSession;

@Getter
@Setter
@Log4j2
public class OperationView implements Serializable {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSession userSession;

    private OperationModel operationModel;

    private OperationFilter filter;

    private List<Department> departments;

    private List<Task> tasks;

    private List<User> users;

    @Transactional
    public void init(OperationModel operationModel) {
        tasks = taskRepository.findFetchProject();
        this.operationModel = operationModel;
        this.operationModel.setFilter(filter.clone());
        if (!userSession.getRights().contains("single-window-operation-log-all")) {
            if (userSession.getRights().contains("single-window-operation-log-group-department")) {
                if (!userSession.getUser().getDepartment().getGroups().isEmpty()) {
                    departments = departmentService.getDepartmentFlatTree(departmentRepository.findAll((root, query, cb) -> {
                        query.distinct(true);
                        return root.join(Department_.groups).in(userSession.getUser().getDepartment().getGroups());
                    }));
                    users = userRepository.findAll((root, query, cb) -> root.join(User_.department).in(departments),
                                                   new Sort(User_.lastname.getName(), User_.firstname.getName(), User_.patronymic.getName()));
                } else {
                    departments = Collections.singletonList(userSession.getUser().getDepartment());
                    users = userRepository.findAll((root, query, cb) -> cb.equal(root.get(User_.department), userSession.getUser().getDepartment()),
                                                   new Sort(User_.lastname.getName(), User_.firstname.getName(), User_.patronymic.getName()));
                }
                this.operationModel.setDefaultDepartments(departments);
                this.operationModel.setDefaultUsers(users);
            } else if (userSession.getRights().contains("single-window-operation-log-department")) {
                users = userRepository.findAll((root, query, cb) -> cb.equal(root.get(User_.department), userSession.getUser().getDepartment()),
                                               new Sort(User_.lastname.getName(), User_.firstname.getName(), User_.patronymic.getName()));
                this.operationModel.setDefaultDepartments(Collections.singletonList(userSession.getUser().getDepartment()));
                this.operationModel.setDefaultUsers(users);
            } else {
                this.operationModel.getFilter().getUsers().add(userSession.getUser());
                this.operationModel.setDefaultDepartments(Collections.singletonList(userSession.getUser().getDepartment()));
            }
        } else {
            users = userRepository.findAll();
            departments = departmentService.getDepartmentFlatTree();
        }
    }

    public void updateFilter() {
        operationModel.setSelected(null);
        operationModel.setFilter(filter.clone());
        operationModel.reset();
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }
}
