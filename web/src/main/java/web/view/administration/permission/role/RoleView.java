/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.role;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import web.repository.core.RoleRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class RoleView implements Serializable, Message {

    @Autowired
    private RoleRepository roleRepository;

    private RoleModel roleModel;

    private RoleItem roleItem;

    public void init(RoleModel roleModel) {
        this.roleModel = roleModel;
    }

    public void delete() {
        try {
            roleRepository.delete(roleItem.getRole());
            roleItem = null;
            roleModel.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:roles")).loadLazyData();
            addInfoMessage("Data successfully saved.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }
}
