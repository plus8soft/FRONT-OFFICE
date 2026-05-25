/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;
import javax.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

@Component
public class TimestampToLocalDateConverter implements AttributeConverter<LocalDate, Timestamp> {

    private static final LocalDate NULL_DATE = LocalDate.of(1900, 1, 1);

    @Override
    public Timestamp convertToDatabaseColumn(LocalDate attribute) {
        return attribute == null ? null : Timestamp.valueOf(attribute.atStartOfDay());
    }

    @Override
    public LocalDate convertToEntityAttribute(Timestamp dbData) {
        return Optional.ofNullable(dbData).map(Timestamp::toLocalDateTime)
                       .map(localDateTime -> NULL_DATE.equals(localDateTime.toLocalDate()) ? null : localDateTime.toLocalDate()).orElse(null);
    }
}
