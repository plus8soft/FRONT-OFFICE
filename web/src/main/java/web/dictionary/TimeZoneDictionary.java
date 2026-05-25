/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import java.time.ZoneId;
import java.util.function.Function;
import org.springframework.stereotype.Repository;
import web.entity.core.DictionaryName;

@Repository
public class TimeZoneDictionary extends AbstractDictionary<ZoneId> {

    @Override
    public DictionaryName getDictionaryName() {
        return DictionaryName.TIME_ZONE;
    }

    @Override
    public Function<String, ZoneId> getKeyFunction() {
        return ZoneId::of;
    }
}
