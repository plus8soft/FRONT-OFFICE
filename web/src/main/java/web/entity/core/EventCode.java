/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EventCode {
    CE_CALCULATING_AMOUNT_EXCHANGE(1000),
    CE_CANCELING_OPERATION(1001),
    CE_PERSON_SEARCH(1002),
    CE_ADDING_PERSON(1003),
    CE_CREATING_PERSON(1004),
    CE_PERSON_MODIFICATION(1005),
    CE_PERSON_MODIFIED(1006),
    CE_OPERATION_STARTED(1007),
    CE_REPORT_REPRINT(1008),
    CE_OPERATION_COMPLETE(1009);

    private static final Map<Integer, EventCode> INDEX = new HashMap<Integer, EventCode>() {{
        Stream.of(EventCode.values()).forEach(eventCode -> put(eventCode.getCode(), eventCode));
    }};

    @Getter
    private int code;

    public static EventCode byCode(Integer code) {
        return INDEX.get(code);
    }
}
