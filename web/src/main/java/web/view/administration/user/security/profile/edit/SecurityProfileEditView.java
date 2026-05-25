/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.security.profile.edit;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.core.SecurityProfile;
import web.repository.core.SecurityProfileRepository;

@Getter
@Setter
@Log4j2
public class SecurityProfileEditView implements Serializable {

    @Autowired
    private SecurityProfileRepository securityProfileRepository;

    private SecurityProfile securityProfile;

    public String save() {
        securityProfileRepository.save(securityProfile);
        return "to-security-profile";
    }
}
