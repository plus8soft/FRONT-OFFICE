/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import web.entity.ce.CurrencyOperation;
import web.entity.core.User;
import web.entity.crm.Document;
import web.entity.crm.DocumentCopy_;
import web.entity.crm.Person;
import web.repository.ce.CurrencyOperationRepository;
import web.repository.ce.RuleRepository;
import web.repository.crm.DocumentCopyRepository;
import web.repository.crm.DocumentRepository;

@Configurable
public class PassportScanCopyValidator implements PersonValidator {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private CurrencyOperationRepository currencyOperationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentCopyRepository documentCopyRepository;

    private CurrencyOperation operation;

    public PassportScanCopyValidator(CurrencyOperation operation) {
        this.operation = operation;
    }

    @Override
    @Transactional
    public void validate(User user, Person person) throws PersonValidateException {
        if (Optional.ofNullable(currencyOperationRepository.getBaseAmountByPersonAndDate(person, operation.getDate().with(LocalTime.MIDNIGHT),
                                                                                     operation.getDate().truncatedTo(ChronoUnit.SECONDS)
                                                                                              .plusSeconds(1))).orElse(BigDecimal.ZERO)
                    .add(operation.getBaseAmount()).compareTo(ruleRepository.findBySystemNameAndSystem("FIN_MON", true).getMin()) >= 0) {
            Document document = documentRepository.findMain(person);
            if (!documentCopyRepository.exists((root, query, cb) -> cb
                    .and(cb.equal(root.get(DocumentCopy_.person), person), cb.equal(root.get(DocumentCopy_.series), document.getSeries()),
                         cb.equal(root.get(DocumentCopy_.number), document.getNumber()),
                         cb.equal(root.get(DocumentCopy_.clientDocumentType), document.getType()),
                         cb.or(cb.greaterThan(root.get(DocumentCopy_.validUntilDate), LocalDate.now(user.getDepartment().getZoneId())),
                               cb.and(cb.isNull(root.get(DocumentCopy_.validUntilDate)), cb.isTrue(root.get(DocumentCopy_.termless))))))) {
                throw new PersonValidateException("Identity document copy is required");
            }
        }
    }
}
