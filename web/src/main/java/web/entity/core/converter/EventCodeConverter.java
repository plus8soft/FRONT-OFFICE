/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import web.entity.core.EventCode;

@Converter(autoApply = true)
public class EventCodeConverter implements AttributeConverter<EventCode, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EventCode attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public EventCode convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : EventCode.byCode(dbData);
    }
}
