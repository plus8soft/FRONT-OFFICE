/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

@Component
public class TimestampToLocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    private static final LocalDateTime NULL_DATE_TIME = LocalDateTime.of(1900, 1, 1, 0, 0);

    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : Timestamp.valueOf(attribute);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp dbData) {
        return Optional.ofNullable(dbData).map(Timestamp::toLocalDateTime)
                       .map(localDateTime -> NULL_DATE_TIME.equals(localDateTime) ? null : localDateTime).orElse(null);
    }
}
