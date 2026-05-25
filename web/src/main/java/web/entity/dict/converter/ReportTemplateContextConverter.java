/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict.converter;

import java.util.Arrays;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import web.entity.dict.ContextType;

@Converter(autoApply = true)
public class ReportTemplateContextConverter implements AttributeConverter<ContextType, String> {

    @Override
    public String convertToDatabaseColumn(ContextType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ContextType convertToEntityAttribute(String dbData) {
        return Arrays.stream(ContextType.values()).filter(context -> context.name().equals(dbData)).findFirst().orElse(null);
    }
}
