/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.settings.module.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.criteria.JoinType;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.core.Group_;
import web.entity.core.Project;
import web.entity.core.Role_;
import web.entity.core.Task;
import web.entity.core.Task_;
import web.entity.core.User_;
import web.repository.core.TaskRepository;
import web.repository.core.UserRepository;
import web.view.DefaultTree;

@Getter
@Setter
public class ModuleUserView implements DefaultTree, Serializable {

    private static final String STATUS_ACTIVE = "ACTIVE";

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private TreeNode treeTask;

    private TreeNode selectedTask;

    public void init() {
        Function<ModuleUserItem, Task> functionModuleUserItemToTask = ModuleUserItem::getTask;
        Function<Project, ModuleUserItem> functionProjectToModuleUserItem = project -> new ModuleUserItem(null, project, 0L, 0L);
        List<ModuleUserItem> collect = taskRepository.findFetchProjectAndChilds().stream()
                                                     .map(task -> new ModuleUserItem(task, null, countActiveBlockedUsers(task, true),
                                                                                     countActiveBlockedUsers(task, false)))
                                                     .collect(Collectors.toList());
        buildGroupingTree(treeTask = new DefaultTreeNode(), functionModuleUserItemToTask.andThen(Task::getParent), functionModuleUserItemToTask,
                          functionModuleUserItemToTask.andThen(Task::getProject), functionProjectToModuleUserItem, collect, false);
        treeTask.getChildren().forEach(treeNode -> {
            ModuleUserItem moduleUserItem = (ModuleUserItem) treeNode.getData();
            moduleUserItem.setActiveCount(countProjectUsers(moduleUserItem.getProject(), true));
            moduleUserItem.setBlockedCount(countProjectUsers(moduleUserItem.getProject(), false));
        });
    }

    private long countActiveBlockedUsers(Task task, boolean isActive) {
        return userRepository.count((root, query, cb) -> {
            query.distinct(true);
            List<Task> tasks = task.getChilds().isEmpty() ? null : getAllChildrenFromTask(task);
            return cb.and(isActive ? cb.equal(root.get(User_.status), STATUS_ACTIVE) : cb.notEqual(root.get(User_.status), STATUS_ACTIVE),
                          tasks == null ? cb.or(cb.isMember(task, root.get(User_.tasks)),
                                                cb.equal(root.join(User_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT), task), cb.equal(
                                          root.join(User_.groups, JoinType.LEFT).join(Group_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT),
                                          task)) : cb.or(root.join(User_.tasks, JoinType.LEFT).in(tasks),
                                                         root.join(User_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT).in(tasks),
                                                         root.join(User_.groups, JoinType.LEFT).join(Group_.roles, JoinType.LEFT)
                                                             .join(Role_.tasks, JoinType.LEFT).in(tasks)));
        });
    }

    private long countProjectUsers(Project project, boolean isActive) {
        return userRepository.count((root, query, cb) -> {
            query.distinct(true);
            return cb.and(isActive ? cb.equal(root.get(User_.status), STATUS_ACTIVE) : cb.notEqual(root.get(User_.status), STATUS_ACTIVE),
                          cb.or(cb.equal(root.join(User_.tasks, JoinType.LEFT).join(Task_.project, JoinType.LEFT), project),
                                cb.equal(root.join(User_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT).join(Task_.project, JoinType.LEFT),
                                         project), cb.equal(
                                          root.join(User_.groups, JoinType.LEFT).join(Group_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT)
                                              .join(Task_.project, JoinType.LEFT), project)));
        });
    }

    private List<Task> getAllChildrenFromTask(Task task) {
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(task.getChilds().stream().filter(taskItem -> {
            if (taskItem.getChilds().isEmpty()) {
                return true;
            } else {
                tasks.addAll(getAllChildrenFromTask(taskItem));
                return false;
            }
        }).collect(Collectors.toList()));
        return tasks;
    }
}
