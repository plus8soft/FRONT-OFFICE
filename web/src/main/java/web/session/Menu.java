/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.session;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.util.Pair;
import org.springframework.webflow.execution.RequestContextHolder;
import web.entity.core.Project;
import web.entity.core.Task;

@Configurable
public class Menu implements Serializable {

    public static final String CS_MENU = "CS_MENU";

    private static final String CLIENT_SEARCH = "menu-single-window-client-search";

    private static final String SINGLE_WINDOW = "menu-single-window";

    private static final String ADMINISTRATION = "menu-administration";

    @Getter
    @Autowired
    private UserSession userSession;

    @Getter
    private Project project;

    @Getter
    private Task task;

    private Map<String, Project> projectNameIndex;

    private Map<Pair<Project, String>, Task> taskNameIndex;

    private Map<Project, Task> startProjectTask;

    public static Menu getInstance() {
        return (Menu) RequestContextHolder.getRequestContext().getConversationScope().get(CS_MENU);
    }

    @PostConstruct
    private void init() {
        Map<Project, Set<Task>> projectTaskIndex = userSession.getTasks().stream().collect(
                Collectors.groupingBy(Task::getProject, LinkedHashMap::new, Collectors.toCollection(LinkedHashSet::new)));
        projectNameIndex = projectTaskIndex.keySet().stream().collect(Collectors.toMap(Project::getSystemName, Function.identity()));
        startProjectTask = projectTaskIndex.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            return SINGLE_WINDOW.equals(entry.getKey().getSystemName()) ?
                   entry.getValue().stream().filter(task -> CLIENT_SEARCH.equals(task.getSystemName())).findFirst()
                        .orElse(entry.getValue().iterator().next()) : entry.getValue().iterator().next();
        }, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        taskNameIndex = userSession.getTasks().stream()
                                   .collect(Collectors.toMap(task -> Pair.of(task.getProject(), task.getSystemName()), Function.identity()));
    }

    public boolean isProjectEnabled(String... names) {
        return Stream.of(names).anyMatch(name -> projectNameIndex.containsKey(name));
    }

    public String getStartProjectName() {
        if (projectNameIndex.containsKey(ADMINISTRATION)) {
            return ADMINISTRATION;
        }
        return startProjectTask.keySet().iterator().next().getSystemName();
    }

    public boolean isSeveralProjects() {
        return startProjectTask.size() > 1;
    }

    public void setProjectByName(String name) {
        project = projectNameIndex.get(name);
    }

    public boolean isTaskEnabled(String... names) {
        return Stream.of(names).anyMatch(name -> taskNameIndex.containsKey(Pair.of(project, name)));
    }

    public String getStartTaskName() {
        return task == null || !task.getProject().equals(project) ? startProjectTask.get(project).getSystemName() : task.getSystemName();
    }

    public void setTaskByName(String name) {
        task = taskNameIndex.get(Pair.of(project, name));
        if (task == null && project != null && "client-search".equals(name)) {
            task = taskNameIndex.get(Pair.of(project, CLIENT_SEARCH));
        }
    }
}
