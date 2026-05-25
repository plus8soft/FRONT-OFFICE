/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.management;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import web.entity.core.Department;
import web.entity.core.SecurityProfile;

@Data
public class UserFilter implements Serializable, Cloneable {

    private String textFilter;

    private SecurityProfile securityProfile;

    private String status;

    private Instant accountExpirationDateFrom;

    private Instant accountExpirationDateTo;

    private Integer position;

    private String phone;

    private String email;

    private List<Department> departments = new ArrayList<>();

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
