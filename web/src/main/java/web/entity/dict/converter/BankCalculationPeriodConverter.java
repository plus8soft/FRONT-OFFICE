/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BankCalculationPeriodConverter implements AttributeConverter<Integer, String> {

    @Override
    public String convertToDatabaseColumn(Integer attribute) {
        return String.valueOf(attribute);
    }

    @Override
    public Integer convertToEntityAttribute(String dbData) {
        return Integer.parseInt(dbData);
    }
}
