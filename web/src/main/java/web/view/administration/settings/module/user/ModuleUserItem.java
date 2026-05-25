/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.settings.module.user;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.Project;
import web.entity.core.Task;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModuleUserItem implements Serializable {

    private Task task;

    private Project project;

    private long activeCount;

    private long blockedCount;
}
