/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.settings.module.user.show;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import web.entity.core.Department;
import web.entity.core.SecurityProfile;
import web.entity.core.Task;

@Data
public class ModuleUserFilter implements Serializable, Cloneable {

    private Task task;

    private String textFilter;

    private Instant expirationDate;

    private List<Department> departmentByNames;

    private List<Department> departmentByCodes;

    private List<String> userStatuses;

    private List<SecurityProfile> securityProfiles;

    private boolean extendedSearch;

    public ModuleUserFilter(Task task) {
        this.task = task;
    }

    @Override
    public ModuleUserFilter clone() {
        try {
            return (ModuleUserFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
