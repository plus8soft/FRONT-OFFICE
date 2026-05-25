/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.role;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleItem implements Serializable {

    private Role role;

    private long usersCount;

    private long groupsCount;

    private long tasksCount;

    private long rightsCount;
}
