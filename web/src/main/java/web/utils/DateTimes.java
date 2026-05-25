/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class DateTimes {

    public static final Map<Long, String> MONTH_OF_YEAR_TEXT = new HashMap<Long, String>() {{
        put(1L, "January");
        put(2L, "February");
        put(3L, "March");
        put(4L, "April");
        put(5L, "May");
        put(6L, "June");
        put(7L, "July");
        put(8L, "August");
        put(9L, "September");
        put(10L, "October");
        put(11L, "November");
        put(12L, "December");
    }};

    public static final LocalDate MIN_LOCAL_DATE = LocalDate.of(1900, 1, 1);

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final DateTimeFormatter ZONED_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss O VV");

    private DateTimes() {
        throw new UnsupportedOperationException();
    }
}
