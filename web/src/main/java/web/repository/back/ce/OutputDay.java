/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.ce;

import lombok.Data;
import web.repository.back.converter.ShortToBooleanConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pFindDayInCalendar_out", index = "XPKfr_pFindDayInCalendar_out")
@Data
public class OutputDay {

    @Column(name = "DayType", converter = ShortToBooleanConverter.class)
    private boolean workday;
}
