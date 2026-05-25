/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.clientedit;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.FileUploadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.dictionary.DocumentTypeDictionary;
import web.dictionary.TermTypeDictionary;
import web.dictionary.Unit;
import web.entity.crm.DocumentCopy;
import web.entity.crm.Person;
import web.entity.dict.DocumentType;
import web.lob.entity.crm.LobDocumentCopy;
import web.lob.repositories.LobDocumentCopyRepository;
import web.repository.crm.DocumentCopyRepository;
import web.repository.dict.DocumentTypeRepository;
import web.session.UserSession;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class EditDocumentCopyView implements Message, Serializable {

    public static final String OTHER_DOCUMENT_TYPE = "99";

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DocumentCopyRepository documentCopyRepository;

    @Autowired
    private LobDocumentCopyRepository lobDocumentCopyRepository;

    @Autowired
    private DocumentTypeDictionary documentTypeDictionary;

    @Autowired
    private TermTypeDictionary termTypeDictionary;

    @Autowired
    private UserSession userSession;

    private DocumentCopy documentCopy;

    private DocumentType documentType;

    private Person person;

    private List<DocumentType> bankDocumentTypes;

    private List<Unit<String>> clientDocumentTypes;

    private Long selectedType;

    private boolean readOnly;

    private String fileExistence;

    public void init(Person person, DocumentCopy documentCopy) {
        this.person = person;
        this.documentCopy = documentCopy;
        bankDocumentTypes = documentTypeRepository.findAll();
        clientDocumentTypes = documentTypeDictionary.findAll();
        if (documentCopy.getBankDocumentType() != null) {
            documentType = documentCopy.getBankDocumentType();
            selectedType = documentType.getId();
        } else {
            documentType = new DocumentType();
            documentType.setTermless(documentCopy.getTermless());
        }
        fileExistence = documentCopy.getFile() == null ? null : "exist";
    }

    public String save() {
        if (documentCopy.getId() == null) {
            documentCopy.setCreationDate(Instant.now());
        }
        documentCopy.setUser(userSession.getUser());
        if (!documentCopy.isExternal()) {
            documentCopy.setBankDocumentType(documentType);
            documentCopy.setName(documentType.getName());
            documentCopy.setVersion(documentCopyRepository.findMaxVersion(person, documentCopy.isExternal(), documentType.getId()).orElse(0) + 1);
        } else {
            documentCopy.setTermless(documentType.getTermless());
            documentCopy.setName(OTHER_DOCUMENT_TYPE.equals(documentCopy.getClientDocumentType()) ? documentCopy.getName() :
                                 clientDocumentTypes.stream().filter(type -> documentCopy.getClientDocumentType().equals(type.getCode())).findAny()
                                                    .map(Unit::getValue).orElse(null));
            documentCopy.setVersion(documentCopyRepository.findMaxVersion(person, documentCopy.isExternal(), documentCopy.getClientDocumentType(),
                                                                          documentCopy.getName()).orElse(0) + 1);
        }
        documentCopy.setPerson(person);
        try {
            saveDocumentCopy();
        } catch (Exception e) {
            addErrorMessage("Service temporarily unavailable. Unable to save copy.");
            return null;
        }
        return back();
    }

    public String back() {
        return readOnly ? "to-client-search" : "to-document-copy";
    }

    public void onDocumentTypeChange() {
        documentCopy.setIssuanceDate(null);
        documentCopy.setTermless(false);
        documentCopy.setValidUntilDate(null);
        if (!documentCopy.isExternal()) {
            documentType = bankDocumentTypes.stream().filter(type -> type.getId().equals(selectedType)).findFirst().orElse(null);
            calculateValidDate();
        } else if (!OTHER_DOCUMENT_TYPE.equals(documentCopy.getClientDocumentType())) {
            documentType.setName(null);
        }
    }

    public void onExternalChange() {
        documentType = new DocumentType();
        documentCopy.setName(null);
        documentCopy.setClientDocumentType(null);
        documentCopy.setBankDocumentType(null);
        documentCopy.setIssuanceDate(null);
        documentCopy.setTermless(false);
        documentCopy.setValidUntilDate(null);
        selectedType = null;
    }

    public void onTermlessChange() {
        if (documentType.getTermless()) {
            documentCopy.setValidUntilDate(null);
            documentType.setTermType(null);
            documentType.setTerm(null);
        } else {
            calculateValidDate();
        }
    }

    public void uploadCopy(FileUploadEvent event) {
        documentCopy.setFileName(createFile(event.getFile().getContents()));
        documentCopy.setFileSize(event.getFile().getSize());
        documentCopy.setFile(event.getFile().getContents());
        fileExistence = "exist";
    }

    private String createFile(byte[] content) {
        try {
            return Files.write(Files.createTempFile(null, null), content).getFileName().toString().replaceFirst("[.][^.]+$", "");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void removeFile() {
        documentCopy.setFileName(null);
        documentCopy.setFileSize(null);
        documentCopy.setFile(null);
        fileExistence = null;
    }

    public void calculateValidDate() {
        if (documentCopy.getIssuanceDate() != null && documentType != null && documentType.getTerm() != null && documentType.getTermType() != null) {
            long term = (long) documentType.getTerm();
            switch (documentType.getTermType()) {
                case DAYS:
                    documentCopy.setValidUntilDate(documentCopy.getIssuanceDate().plusDays(term));
                    break;
                case WEEKS:
                    documentCopy.setValidUntilDate(documentCopy.getIssuanceDate().plusWeeks(term));
                    break;
                case MONTHS:
                    documentCopy.setValidUntilDate(documentCopy.getIssuanceDate().plusMonths(term));
                    break;
                default:
                    documentCopy.setValidUntilDate(documentCopy.getIssuanceDate());
                    break;
            }
        }
    }

    public void onValidUntilDateChange() {
        documentType.setTerm(null);
        documentType.setTermType(null);
    }

    @Transactional
    private void saveDocumentCopy() {
        lobDocumentCopyRepository.save(new LobDocumentCopy(documentCopyRepository.save(documentCopy).getId(), documentCopy.getFile()));
    }
}
