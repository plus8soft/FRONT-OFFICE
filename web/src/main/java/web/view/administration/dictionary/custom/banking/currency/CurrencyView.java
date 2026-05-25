/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.currency;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import web.entity.dict.Currency;
import web.entity.dict.Currency_;
import web.entity.dict.DictionaryParameter;
import web.repository.dict.CurrencyRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class CurrencyView implements Message, Serializable {

    @Autowired
    private CurrencyRepository currencyRepository;

    private DictionaryParameter dictionary;

    private List<Currency> currencies;

    private Currency selectedCurrency;

    private byte[] emptyIcon;

    public void init() throws URISyntaxException, IOException {
        emptyIcon = Files.readAllBytes(Paths.get(getClass().getResource("/image/no-icon-currency.png").toURI()));
        currencies = currencyRepository.findAll(new Sort(Currency_.position.getName()));
    }

    public Currency add() {
        Currency currency = new Currency();
        currency.setImage(emptyIcon);
        return currency;
    }

    public Currency edit() throws URISyntaxException, IOException {
        Currency currency = new Currency();
        BeanUtils.copyProperties(selectedCurrency, currency);
        return currency;
    }

    public void onRowReorder() {
        try {
            IntStream.range(0, currencies.size()).forEach(position -> currencies.get(position).setPosition(position));
            currencyRepository.save(currencies);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error when saving data.");
        }
    }

    public void delete() {
        try {
            currencyRepository.delete(currencies);
            init();
            addInfoMessage("Data saved successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }
}
