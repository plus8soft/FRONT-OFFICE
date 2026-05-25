/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.documenttype;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.DocumentTypeRepository;
import web.view.Message;

@Log4j2
@Getter
@Setter
public class DocumentTypeView implements Message, Serializable {

    public static final Map<ChronoUnit, String> TERM_TYPE_MAP = new HashMap<ChronoUnit, String>() {
        {
            put(ChronoUnit.MINUTES, "Minutes");
            put(ChronoUnit.HOURS, "Hours");
            put(ChronoUnit.DAYS, "Days");
            put(ChronoUnit.MONTHS, "Months");
        }
    };

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private DictionaryParameter dictionary;

    private DocumentTypeModel model;

    public void init(DocumentTypeModel model, DictionaryParameter dictionary) {
        this.dictionary = dictionary;
        this.model = model;
    }

    public void delete() {
        try {
            documentTypeRepository.delete(model.getSelected());
            model.setSelected(null);
            model.reset();
            ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:doctypes")).loadLazyData();
            addInfoMessage("Data deleted successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while deleting data.");
        }
    }
}
