/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import java.util.function.Function;
import org.springframework.stereotype.Repository;
import web.entity.core.DictionaryName;
import web.entity.core.EventStatus;

@Repository
public class EventStatusDictionary extends AbstractDictionary<EventStatus> {

    @Override
    public DictionaryName getDictionaryName() {
        return DictionaryName.EVENT_TYPE;
    }

    @Override
    public Function<String, EventStatus> getKeyFunction() {
        return value -> EventStatus.values()[Integer.valueOf(value)];
    }
}
