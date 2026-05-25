/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.system;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.PaymentSystem;
import web.repository.dict.PaymentSystemRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class EditView implements Message, Serializable {

    @Autowired
    private PaymentSystemRepository paymentSystemRepository;

    private DictionaryParameter dictionary;

    private PaymentSystem paymentSystem;

    public String save() {
        try {
            paymentSystemRepository.save(paymentSystem);
            addInfoMessage("Data saved successfully.");
            return "save";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            return null;
        }
    }
}
