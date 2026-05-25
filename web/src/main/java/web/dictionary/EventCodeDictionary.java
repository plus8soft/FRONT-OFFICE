/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import java.util.function.Function;
import org.springframework.stereotype.Repository;
import web.entity.core.DictionaryName;
import web.entity.core.EventCode;

@Repository
public class EventCodeDictionary extends AbstractDictionary<EventCode> {

    @Override
    public DictionaryName getDictionaryName() {
        return DictionaryName.EVENT_CODE;
    }

    @Override
    public Function<String, EventCode> getKeyFunction() {
        return value -> EventCode.byCode(Integer.valueOf(value));
    }
}
