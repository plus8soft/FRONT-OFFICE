/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import java.util.function.Function;
import org.springframework.stereotype.Repository;
import web.entity.core.DictionaryName;

@Repository
public class NdsDictionary extends AbstractDictionary<Integer> {

    @Override
    public DictionaryName getDictionaryName() {
        return DictionaryName.NDS;
    }

    @Override
    public Function<String, Integer> getKeyFunction() {
        return Integer::valueOf;
    }
}
