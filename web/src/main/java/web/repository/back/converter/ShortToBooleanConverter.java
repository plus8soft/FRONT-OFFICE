/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import java.util.Optional;
import javax.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

@Component
public class ShortToBooleanConverter implements AttributeConverter<Boolean, Short> {

    @Override
    public Short convertToDatabaseColumn(Boolean attribute) {
        return attribute != null && attribute ? (short) 1 : (short) 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Short dbData) {
        return Optional.ofNullable(dbData).map(value -> value == 1).orElse(false);
    }
}
