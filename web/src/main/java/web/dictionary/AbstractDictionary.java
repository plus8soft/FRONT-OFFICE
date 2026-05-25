/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import web.entity.core.DictionaryName;

@Lazy
public abstract class AbstractDictionary<K> {

    @Autowired
    private DictionaryValueCache dictionaryValueCache;

    public abstract DictionaryName getDictionaryName();

    public Function<String, K> getKeyFunction() {
        return s -> (K) s;
    }

    public Unit<K> findOne(K code) {
        return dictionaryValueCache.findOne(getDictionaryName(), getKeyFunction(), code);
    }

    public List<Unit<K>> findAll() {
        return dictionaryValueCache.findAll(getDictionaryName(), getKeyFunction());
    }

    public List<Unit<K>> findAll(K... exclusions) {
        return dictionaryValueCache.findAll(getDictionaryName(), getKeyFunction(), exclusions);
    }
}
