/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import org.springframework.stereotype.Repository;
import web.entity.core.DictionaryName;

@Repository
public class ClientStatusDictionary extends AbstractDictionary<String> {

    @Override
    public DictionaryName getDictionaryName() {
        return DictionaryName.CLIENT_STATUS;
    }
}
