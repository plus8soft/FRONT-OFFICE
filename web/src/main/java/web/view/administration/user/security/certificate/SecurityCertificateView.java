/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.security.certificate;

import java.io.Serializable;
import java.util.List;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Department;
import web.entity.core.SecurityProfile;
import web.repository.core.SecurityProfileRepository;
import web.service.administration.department.DepartmentService;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class SecurityCertificateView implements Message, Serializable {

    @Autowired
    private SecurityProfileRepository securityProfileRepository;

    @Autowired
    private DepartmentService departmentService;

    private SecurityCertificateFilter filter;

    private SecurityCertificateModel model;

    private List<SecurityProfile> securityProfiles;

    private List<Department> departments;

    @Transactional
    public void init(SecurityCertificateModel securityCertificateModel) {
        securityProfiles = securityProfileRepository.findAll();
        departments = departmentService.getDepartmentFlatTree();
        filter = new SecurityCertificateFilter();
        model = securityCertificateModel;
        model.setFilter(filter.clone());
    }

    public void fastSearch() {
        filter.setExtendedSearch(false);
        updateFilter();
    }

    public void extendedSearch() {
        filter.setExtendedSearch(true);
        updateFilter();
    }

    private void updateFilter() {
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:certificates")).reset();
    }
}
