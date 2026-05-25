/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import javax.persistence.AttributeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.dict.Currency;
import web.entity.dict.Currency_;
import web.repository.dict.CurrencyRepository;

@Component
public class ShortToCurrencyConverter implements AttributeConverter<Currency, String> {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Override
    public String convertToDatabaseColumn(Currency attribute) {
        return attribute == null ? null : attribute.getIso();
    }

    @Override
    public Currency convertToEntityAttribute(String dbData) {
        return dbData == null ? null : currencyRepository.findOne((root, query, cb) -> cb.equal(root.get(Currency_.id), dbData));
    }
}
