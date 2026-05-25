/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.documenttype;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.DocumentType;
import web.entity.dict.ReportTemplate;
import web.entity.dict.ReportTemplate_;
import web.repository.dict.DocumentTypeRepository;
import web.repository.dict.ReportTemplateRepository;
import web.view.Message;

@Log4j2
@Getter
@Setter
public class DocumentTypeEditView implements Message, Serializable {

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    private DocumentType documentType;

    private DictionaryParameter dictionary;

    private List<ReportTemplate> templates;

    public void init(DocumentType documentType, DictionaryParameter dictionary) {
        this.documentType = documentType;
        this.dictionary = dictionary;
        templates = reportTemplateRepository.findAll((root, query, cb) -> cb.isNull(root.get(ReportTemplate_.systemName)));
    }

    public String save() {
        try {
            documentTypeRepository.save(documentType);
            addInfoMessage("Data saved successfully.");
            return "save";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            return null;
        }
    }

    public void onTermlessChanged() {
        if (documentType.getTermless()) {
            documentType.setTermType(null);
            documentType.setTerm(null);
        }
    }
}
