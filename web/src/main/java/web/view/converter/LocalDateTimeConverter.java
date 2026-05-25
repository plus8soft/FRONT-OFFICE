/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import java.time.LocalDateTime;
import java.time.temporal.TemporalQuery;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "javax.faces.LocalDateTimeConverter")
public class LocalDateTimeConverter extends AbstractDateTimeConverter {

    @Override
    protected TemporalQuery getTemporalQuery() {
        return LocalDateTime::from;
    }
}
