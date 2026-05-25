/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.clientedit;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.JoinType;
import javax.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.event.CloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.crm.DocumentCopy;
import web.entity.crm.DocumentCopy_;
import web.entity.crm.Person;
import web.lob.entity.crm.LobDocumentCopy;
import web.lob.repositories.LobDocumentCopyRepository;
import web.repository.crm.DocumentCopyRepository;
import web.repository.crm.PersonRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class DocumentCopyView implements Serializable, Message {

    @Autowired
    private DocumentCopyRepository documentCopyRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private LobDocumentCopyRepository lobDocumentCopyRepository;

    private List<DocumentCopy> documentCopies;

    private DocumentCopy selected;

    private boolean showReport;

    public void init(Person person) {
        documentCopies = documentCopyRepository.findAll((root, query, cb) -> {
            root.fetch(DocumentCopy_.bankDocumentType, JoinType.LEFT);
            return cb.equal(root.get(DocumentCopy_.person), person);
        }).stream().map(documentCopy -> {
            LobDocumentCopy lobDocumentCopy = lobDocumentCopyRepository.findOne(documentCopy.getId());
            if (lobDocumentCopy != null) {
                documentCopy.setFile(lobDocumentCopy.getData());
            } else {
                addErrorMessageFormated("Scan copy file for \"{0}\" is lost", documentCopy.getName());
            }
            return documentCopy;
        }).collect(Collectors.toList());
    }

    public void delete() {
        try {
            if (selected.getCreationDate().plus(1, ChronoUnit.DAYS).isAfter(Instant.now())) {
                if (documentCopies.remove(selected)) {
                    Files.delete(Paths.get(
                            String.format("%s%s%s%s", System.getProperty("java.io.tmpdir"), File.separator, selected.getFileName(), ".tmp")));
                    deleteSelectedCopy();
                    selected = null;
                    addInfoMessage("Data successfully deleted.");
                }
            } else {
                addInfoMessage("Deletion is not possible after the first 24 hours.");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while deleting data.");
        }
    }

    public void closeDialog(CloseEvent event) {
        showReport = false;
        ((Dialog) event.getComponent()).setVisible(true);
    }

    @Transactional
    private void deleteSelectedCopy() {
        documentCopyRepository.delete(selected);
        lobDocumentCopyRepository.delete(selected.getId());
    }
}
