/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.audit.management;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.core.EventSetting_;
import web.entity.core.Project;
import web.entity.core.Task;
import web.repository.core.EventSettingRepository;
import web.repository.core.TaskRepository;
import web.view.DefaultTree;

@Getter
@Setter
@Log4j2
public class EventSettingView implements DefaultTree, Serializable {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EventSettingRepository eventSettingRepository;

    private TreeNode treeTask;

    private TreeNode selectedTask;

    public void init() {
        List<TaskItem> collect = taskRepository.findFetchProject().stream().map(this::buildTaskItem).collect(Collectors.toList());
        buildGroupingTree(treeTask = new DefaultTreeNode(), taskItem -> taskItem.getTask().getParent(), TaskItem::getTask,
                          taskItem -> taskItem.getTask().getProject(), project -> new TaskItem(((Project) project).getName(), null, 0, 0), collect,
                          false);
        streamTree(treeTask).skip(1).filter(treeNode -> !treeNode.isLeaf()).forEach(treeNode -> {
            TaskItem taskItem = (TaskItem) treeNode.getData();
            taskItem.setEventsCount(streamTree(treeNode).mapToLong(node -> ((TaskItem) node.getData()).getEventsCount()).sum());
            taskItem.setAviableEventsCount(streamTree(treeNode).mapToLong(node -> ((TaskItem) node.getData()).getAviableEventsCount()).sum());
        });
    }

    private TaskItem buildTaskItem(Task task) {
        TaskItem taskItem = new TaskItem();
        taskItem.setName(task.getName());
        taskItem.setTask(task);
        taskItem.setEventsCount(eventSettingRepository.count((root, query, cb) -> cb.equal(root.get(EventSetting_.task), task)));
        taskItem.setAviableEventsCount(taskItem.getEventsCount() == 0 ? 0 : eventSettingRepository
                .count((root, query, cb) -> cb.and(cb.equal(root.get(EventSetting_.task), task), cb.isTrue(root.get(EventSetting_.enabled)))));
        return taskItem;
    }
}
