/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.security.certificate;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import web.entity.core.Department;
import web.entity.core.SecurityProfile;

@Data
public class SecurityCertificateFilter implements Serializable, Cloneable {

    private String textFilter;

    private Instant startDate;

    private Instant endDate;

    private String serialNumber;

    private Boolean locked;

    private Instant expirationDate;

    private List<Department> departmentByNames;

    private List<Department> departmentByCodes;

    private List<String> userStatuses;

    private List<SecurityProfile> securityProfiles;

    private boolean extendedSearch;

    @Override
    public SecurityCertificateFilter clone() {
        try {
            return (SecurityCertificateFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
