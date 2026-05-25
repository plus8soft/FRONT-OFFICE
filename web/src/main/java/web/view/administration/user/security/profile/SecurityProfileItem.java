/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.security.profile;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import web.entity.core.SecurityProfile;

@Getter
@Setter
@AllArgsConstructor
public class SecurityProfileItem implements Serializable {

    private SecurityProfile securityProfile;

    private long usersCount;
}
