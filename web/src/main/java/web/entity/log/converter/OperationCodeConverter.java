/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.log.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import web.entity.log.OperationCode;

@Converter(autoApply = true)
public class OperationCodeConverter implements AttributeConverter<OperationCode, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OperationCode attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public OperationCode convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : OperationCode.byCode(dbData);
    }
}
