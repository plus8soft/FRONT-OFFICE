/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.currency;

import java.io.Serializable;
import java.util.Base64;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.FileUploadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.Currency;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.CurrencyRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class EditView implements Message, Serializable {

    @Autowired
    private CurrencyRepository currencyRepository;

    private DictionaryParameter dictionary;

    private Currency currency;

    private boolean editing;

    public String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes == null ? new byte[]{} : bytes);
    }

    public void uploadIcon(FileUploadEvent event) {
        currency.setImage(event.getFile().getContents());
    }

    public String save() {
        try {
            if (!editing) {
                currency.setPosition(currencyRepository.findLastPosition().map(position -> position + 1).orElse(0));
            }
            currencyRepository.save(currency);
            addInfoMessage("Data saved successfully.");
            return "save";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
            return null;
        }
    }
}
