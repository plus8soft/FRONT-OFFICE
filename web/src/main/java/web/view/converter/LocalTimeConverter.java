/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import java.time.LocalTime;
import java.time.temporal.TemporalQuery;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "javax.faces.LocalTimeConverter")
public class LocalTimeConverter extends AbstractDateTimeConverter {

    @Override
    protected TemporalQuery getTemporalQuery() {
        return LocalTime::from;
    }
}
