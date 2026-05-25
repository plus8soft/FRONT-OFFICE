/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.converter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import javax.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

@Component
public class TimestampToLocalTimeConverter implements AttributeConverter<LocalTime, Timestamp> {

    private static final LocalDate NULL_DATE = LocalDate.of(1900, 1, 1);

    @Override
    public Timestamp convertToDatabaseColumn(LocalTime attribute) {
        return attribute == null ? null : Timestamp.valueOf(NULL_DATE.atTime(attribute));
    }

    @Override
    public LocalTime convertToEntityAttribute(Timestamp dbData) {
        return Optional.ofNullable(dbData).map(timestamp -> timestamp.toLocalDateTime().toLocalTime()).orElse(null);
    }
}
