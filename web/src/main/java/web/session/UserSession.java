/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.session;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import web.entity.core.Department;
import web.entity.core.Task;
import web.entity.core.User;
import web.entity.log.ConnectionEvent;

@Component
@Scope(WebApplicationContext.SCOPE_SESSION)
@Data
public class UserSession implements Serializable {

    private boolean viaToken;

    private ConnectionEvent connectionEvent;

    private User user;

    private List<Department> departmentsGraph;

    private Set<Department> departments;

    private List<Task> tasks;

    private Set<String> rights;

    private String theme = "plus8theme";

    public boolean isAuthenticated() {
        return user != null;
    }
}
