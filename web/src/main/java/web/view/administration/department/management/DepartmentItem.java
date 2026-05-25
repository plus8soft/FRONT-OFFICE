/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.department.management;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.Department;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentItem implements Serializable {

    private Department department;

    private long usersCount;
}
