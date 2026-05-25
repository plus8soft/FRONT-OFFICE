/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import javax.persistence.AttributeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.entity.dict.Country_;
import web.repository.dict.CountryRepository;

@Component
public class CountryAlpha2ToIsoConverter implements AttributeConverter<String, String> {

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : countryRepository.findOne(attribute).getAlpha2();
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : countryRepository.findOne((root, query, cb) -> cb.equal(root.get(Country_.alpha2), dbData)).getId();
    }
}
