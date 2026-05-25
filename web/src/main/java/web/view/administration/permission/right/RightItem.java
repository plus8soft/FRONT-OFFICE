/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.right;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.Right;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RightItem implements Serializable {

    private Right right;

    private long usersCount;

    private long rolesCount;
}
