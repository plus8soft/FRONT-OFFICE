/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import java.time.LocalDate;
import java.time.temporal.TemporalQuery;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "javax.faces.LocalDateConverter")
public class LocalDateConverter extends AbstractDateTimeConverter {

    @Override
    protected TemporalQuery getTemporalQuery() {
        return LocalDate::from;
    }
}
