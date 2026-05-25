/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.security.profile;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.repository.core.SecurityProfileRepository;
import web.repository.core.UserRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class SecurityProfileView implements Serializable, Message {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityProfileRepository securityProfileRepository;

    private SecurityProfileModel securityProfileModel;

    private SecurityProfileItem selected;

    public void init(SecurityProfileModel securityProfileModel) {
        this.securityProfileModel = securityProfileModel;
    }

    @Transactional
    public void delete() {
        if (selected.getUsersCount() == 0) {
            securityProfileRepository.delete(selected.getSecurityProfile());
            selected = null;
            securityProfileModel.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:security-profile")).loadLazyData();
            addInfoMessage("Data deleted successfully.");
        } else {
            addErrorMessage("Error deleting data.");
        }
    }
}
