/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.core;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import web.entity.core.Task;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t JOIN FETCH t.project")
    List<Task> findFetchProject();

    @Query("SELECT DISTINCT t FROM Task t JOIN FETCH t.project LEFT JOIN FETCH t.childs")
    List<Task> findFetchProjectAndChilds();
}
