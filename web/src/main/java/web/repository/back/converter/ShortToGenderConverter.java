/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import javax.persistence.AttributeConverter;
import org.springframework.stereotype.Component;
import web.entity.crm.Gender;

@Component
public class ShortToGenderConverter implements AttributeConverter<Gender, Short> {

    @Override
    public Short convertToDatabaseColumn(Gender attribute) {
        return attribute == null ? null : Gender.MALE.equals(attribute) ? (short) 0 : 1;
    }

    @Override
    public Gender convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : 1 == dbData ? Gender.FEMALE : Gender.MALE;
    }
}
