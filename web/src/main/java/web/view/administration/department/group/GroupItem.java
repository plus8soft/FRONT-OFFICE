/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.department.group;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.Group;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupItem implements Serializable {

    private Group group;

    private long departmentsCount;

    private long usersCount;
}
