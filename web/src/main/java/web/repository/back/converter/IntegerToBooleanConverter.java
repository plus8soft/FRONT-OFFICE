/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import javax.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

@Component
public class IntegerToBooleanConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        return attribute == null || !attribute ? 0 : 1;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        return !(dbData == null || dbData == 0);
    }
}
