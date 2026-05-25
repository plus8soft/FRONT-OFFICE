/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.jaxb;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantAdapter extends TemporalAccessorXmlAdapter<Instant> {

    public InstantAdapter() {
        super(DateTimeFormatter.ISO_INSTANT, Instant::from);
    }
}
