/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.audit.management;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.entity.core.Task;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskItem implements Serializable {

    private String name;

    private Task task;

    private long eventsCount;

    private long aviableEventsCount;
}
