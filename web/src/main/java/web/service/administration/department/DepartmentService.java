/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.administration.department;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.repository.core.DepartmentRepository;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> getDepartmentFlatTree() {
        return getDepartmentFlatTree(departmentRepository.findAll());
    }

    public List<Department> getDepartmentFlatTree(Collection<Department> departments) {
        List<Department> result = new ArrayList<>();
        buildDepartmentFlatTree(null, new LinkedList<>(departments), result);
        return result;
    }

    @Transactional
    public List<Department> getDepartmentsGraphFromChilds(Set<Department> childs) {
        Set<Department> result = new HashSet<>(childs);
        childs.parallelStream().map(Department::getParent).filter(Objects::nonNull).forEach(department -> {
            do {
                result.add(department);
                department = department.getParent();
            } while (department != null);
        });
        return result.stream().collect(Collectors.toList());
    }

    private void buildDepartmentFlatTree(Department parent, LinkedList<Department> departments, List<Department> result) {
        if (parent != null) {
            departments.remove(parent);
            result.add(parent);
        }
        departments.stream().filter(department -> Objects.equals(parent, department.getParent()))
                   .sorted(Comparator.comparing(Department::getPosition))
                   .forEachOrdered(department -> buildDepartmentFlatTree(department, departments, result));
    }
}
