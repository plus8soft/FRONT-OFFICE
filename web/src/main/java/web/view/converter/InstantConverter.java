/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import java.time.Instant;
import java.time.temporal.TemporalQuery;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "javax.faces.Instant")
public class InstantConverter extends AbstractDateTimeConverter {

    @Override
    protected TemporalQuery getTemporalQuery() {
        return Instant::from;
    }
}
