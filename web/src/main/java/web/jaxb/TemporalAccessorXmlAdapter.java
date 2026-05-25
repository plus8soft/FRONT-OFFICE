/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.jaxb;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TemporalAccessorXmlAdapter<T extends TemporalAccessor> extends XmlAdapter<String, T> {

    private final DateTimeFormatter formatter;

    private final TemporalQuery<? extends T> temporalQuery;

    public TemporalAccessorXmlAdapter(DateTimeFormatter formatter, TemporalQuery<? extends T> temporalQuery) {
        this.formatter = formatter;
        this.temporalQuery = temporalQuery;
    }

    @Override
    public T unmarshal(String stringValue) {
        return stringValue != null ? formatter.parse(stringValue, temporalQuery) : null;
    }

    @Override
    public String marshal(T value) {
        return value != null ? formatter.format(value) : null;
    }
}
