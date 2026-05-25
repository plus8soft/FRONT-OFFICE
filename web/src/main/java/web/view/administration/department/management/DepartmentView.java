/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.department.management;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import web.entity.core.Department;
import web.entity.core.Department_;
import web.repository.core.DepartmentRepository;
import web.repository.core.UserRepository;
import web.repository.core.projection.DepartmentIdUsersCount;
import web.view.DefaultTree;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class DepartmentView implements DefaultTree, Message, Serializable {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    private TreeNode tree;

    private TreeNode selected;

    private TreeNode sourceNode;

    private TreeNode dragNode;

    private TreeNode dropNode;

    private boolean enabled;

    public void init() {
        Function<DepartmentItem, Department> function = DepartmentItem::getDepartment;
        Map<Long, Long> map = departmentRepository.countUsers().stream()
                                                  .collect(Collectors.toMap(DepartmentIdUsersCount::getId, DepartmentIdUsersCount::getUsersCount));
        buildTreeWithEmptyMessage(tree = new DefaultTreeNode(), function.andThen(Department::getParent), function,
                                  function.andThen(Department::getPosition),
                                  departmentRepository.findAll((root, query, cb) -> enabled ? cb.isTrue(root.get(Department_.enabled)) : null)
                                                      .stream().map(department -> {
                                      DepartmentItem departmentItem = new DepartmentItem();
                                      departmentItem.setDepartment(department);
                                      departmentItem.setUsersCount(Optional.ofNullable(map.get(department.getId())).orElse(0L));
                                      return departmentItem;
                                  }).collect(Collectors.toList()));
    }

    public void onDragDrop(TreeDragDropEvent event) {
        sourceNode = defineSourceNode(tree, event);
        dragNode = event.getDragNode();
        dropNode = event.getDropNode();
    }

    public void confirmDragDrop() {
        DepartmentItem dropNodeDepartmentItem = ((DepartmentItem) dropNode.getData());
        if (dropNodeDepartmentItem == null || (dropNodeDepartmentItem.getDepartment().isEnabled() &&
                                               departmentRepository.countNotActiveParents(dropNodeDepartmentItem.getDepartment().getId()) == 0)) {
            IntStream.range(0, sourceNode.getChildren().size()).forEachOrdered(index -> {
                Department child = ((DepartmentItem) sourceNode.getChildren().get(index).getData()).getDepartment();
                child.setPosition(index);
                departmentRepository.save(child);
            });
            ((DepartmentItem) dragNode.getData()).getDepartment()
                                                 .setParent(dropNodeDepartmentItem == null ? null : dropNodeDepartmentItem.getDepartment());
            IntStream.range(0, dropNode.getChildren().size()).forEachOrdered(index -> {
                Department child = ((DepartmentItem) dropNode.getChildren().get(index).getData()).getDepartment();
                child.setPosition(index);
                departmentRepository.save(child);
            });
        } else {
            cancelDragDrop();
            addErrorMessage("Cannot add. There are inactive parent elements");
        }
    }

    public void cancelDragDrop() {
        dropNode.getChildren().remove(dragNode);
        sourceNode.getChildren().add(((DepartmentItem) dragNode.getData()).getDepartment().getPosition(), dragNode);
    }

    public Department add() {
        Department department = new Department();
        department.setParent(selected == null ? null : ((DepartmentItem) selected.getData()).getDepartment());
        department.setPosition(selected == null ? tree.getChildCount() : selected.getChildCount());
        return department;
    }

    public void delete() {
        try {
            departmentRepository.delete(((DepartmentItem) selected.getData()).getDepartment());
            List<TreeNode> children =
                    selected.getParent().getChildren().stream().filter(child -> !child.equals(selected)).collect(Collectors.toList());
            if (selected.getParent().getChildren().remove(selected)) {
                IntStream.range(0, children.size()).forEachOrdered(index -> {
                    Department child = ((DepartmentItem) children.get(index).getData()).getDepartment();
                    child.setPosition(index);
                    departmentRepository.save(child);
                });
            }
            selected = null;
            if (tree.isLeaf()) {
                addEmptyMessage(tree, "No data");
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Cannot delete organization structure node, there are linked objects.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while deleting data.");
        }
    }

    public String tryAdd() {
        if (selected != null && (!((DepartmentItem) selected.getData()).getDepartment().isEnabled() ||
                                 departmentRepository.countNotActiveParents(((DepartmentItem) selected.getData()).getDepartment().getId()) > 0)) {
            addErrorMessage("Cannot add. There are inactive parent elements");
            return null;
        } else {
            return "add";
        }
    }
}
