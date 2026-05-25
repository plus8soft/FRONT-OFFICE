/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.log;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum OperationCode {
    SELL(0),
    BUY(1),
    SEND(10),
    ISSUE(11),
    CANCEL(12),
    COMPANY_PARTNER_PAYMENT(20),
    COMPANY_FREE_PAYMENT(21),
    PUBLIC_SECTOR_PAYMENT(22),
    TAXES_PAYMENT(23);

    private static final Map<Integer, OperationCode> INDEX = new HashMap<Integer, OperationCode>() {{
        Stream.of(OperationCode.values()).forEach(operationCode -> put(operationCode.getCode(), operationCode));
    }};

    @Getter
    private int code;

    public static OperationCode byCode(Integer code) {
        return INDEX.get(code);
    }
}
