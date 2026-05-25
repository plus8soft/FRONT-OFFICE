/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.reporttemplate;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.ReportTemplateRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class ReportTemplateView implements Message, Serializable {

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    private ReportTemplateModel model;

    private DictionaryParameter dictionary;

    public void init(ReportTemplateModel model, DictionaryParameter dictionary) {
        this.model = model;
        this.dictionary = dictionary;
    }

    public void delete() {
        try {
            reportTemplateRepository.delete(model.getSelected());
            model.setSelected(null);
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:report-templates")).loadLazyData();
            addInfoMessage("Data deleted successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while deleting data.");
        }
    }
}
