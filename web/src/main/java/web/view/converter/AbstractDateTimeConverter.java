/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.TimeZone;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import web.view.Message;

public abstract class AbstractDateTimeConverter implements Converter, Message {

    protected DateTimeFormatter getFormatter(FacesContext context, UIComponent component) {
        String pattern = (String) component.getAttributes().get("pattern");
        return (pattern == null ? DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL) :
                new DateTimeFormatterBuilder().appendPattern(pattern).parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                                              .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                                              .parseDefaulting(ChronoField.NANO_OF_SECOND, 0).toFormatter()).withLocale(getLocale(context, component))
                                                                                                            .withZone(getZoneId(component));
    }

    private Locale getLocale(FacesContext context, UIComponent component) {
        Object locale = component.getAttributes().get("locale");
        return (locale instanceof Locale) ? (Locale) locale :
               (locale instanceof String) ? new Locale((String) locale) : context.getViewRoot().getLocale();
    }

    private ZoneId getZoneId(UIComponent component) {
        Object zoneId = component.getAttributes().get("timeZone");
        return (zoneId instanceof ZoneId) ? ((ZoneId) zoneId) : (zoneId instanceof TimeZone) ? ((TimeZone) zoneId).toZoneId() :
                                                                (zoneId instanceof String) ? ZoneId.of((String) zoneId) : ZoneId.systemDefault();
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        if (value != null) {
            try {
                result = getFormatter(context, component).parse(value, getTemporalQuery());
            } catch (DateTimeParseException e) {
                throw new ConverterException(errorMessage("Invalid date"), e);
            }
        }
        return result;
    }

    protected abstract TemporalQuery getTemporalQuery();

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? null : getFormatter(context, component).format((TemporalAccessor) value);
    }
}
