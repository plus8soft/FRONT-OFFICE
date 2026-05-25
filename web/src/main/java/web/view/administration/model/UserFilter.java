/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.model;

import java.io.Serializable;
import java.time.Instant;
import lombok.Data;
import web.entity.core.Department;
import web.entity.core.Group;
import web.entity.core.Right;
import web.entity.core.Role;

@Data
public class UserFilter implements Serializable, Cloneable {

    private String textFilter;

    private String status;

    private Integer position;

    private Group userGroup;

    private Department department;

    private Group departmentGroup;

    private Instant lastLoginEventDate;

    private Role role;

    private Right right;

    private boolean extendedSearch;

    @Override
    public UserFilter clone() {
        try {
            return (UserFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
