/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import java.util.Optional;
import javax.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

@Component
public class StringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return Optional.ofNullable(dbData).map(String::trim).map(string -> string.isEmpty() ? null : string).orElse(null);
    }
}
