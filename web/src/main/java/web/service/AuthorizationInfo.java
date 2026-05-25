/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.Data;
import web.entity.core.Department;
import web.entity.core.Task;
import web.entity.core.User;
import web.entity.log.ConnectionEvent;

@Data
public class AuthorizationInfo implements Serializable {

    private ConnectionEvent connectionEvent;

    private User user;

    private Instant date;

    private List<Department> departmentsGraph;

    private Set<Department> departments;

    private List<Task> tasks;

    private Set<String> rights;
}
