/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.management;

import java.io.Serializable;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.entity.core.SecurityProfile;
import web.repository.core.SecurityProfileRepository;
import web.repository.core.UserRepository;
import web.service.administration.department.DepartmentService;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class UserView implements Message, Serializable {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityProfileRepository securityProfileRepository;

    @Autowired
    private DepartmentService departmentService;

    private UserFilter filter;

    private UserModel model;

    private List<Department> departments;

    private List<SecurityProfile> securityProfiles;

    @Transactional
    public void init(UserModel userModel) {
        filter.setTextFilter(null);
        departments = departmentService.getDepartmentFlatTree();
        model = userModel;
        model.setFilter(filter.clone());
        securityProfiles = securityProfileRepository.findAll();
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }

    private void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:users")).reset();
    }

    public void delete() {
        try {
            userRepository.delete(model.getSelected());
            model.setSelected(null);
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:users")).loadLazyData();
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage(), e);
            addErrorMessage("User deletion is not possible because there are related objects.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while deleting data.");
        }
    }

    public void setStatusForSelectedUser(String status) {
        model.getSelected().setStatus(status);
        userRepository.save(model.getSelected());
    }
}
