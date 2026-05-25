/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.jaxb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TemporalAccessorXmlAdapter<LocalDate> {

    public LocalDateAdapter() {
        super(DateTimeFormatter.ISO_LOCAL_DATE, LocalDate::from);
    }
}
