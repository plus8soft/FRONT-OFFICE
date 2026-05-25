/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import java.math.BigDecimal;
import javax.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

@Component
public class BigDecimalToLongConverter implements AttributeConverter<Long, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Long attribute) {
        return attribute == null ? null : BigDecimal.valueOf(attribute);
    }

    @Override
    public Long convertToEntityAttribute(BigDecimal dbData) {
        return dbData == null ? null : dbData.longValue();
    }
}
