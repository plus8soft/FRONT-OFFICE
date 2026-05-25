/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import org.springframework.stereotype.Repository;
import web.entity.core.DictionaryName;

@Repository
public class ReportTemplateDictionary extends AbstractDictionary<String> {

    @Override
    public DictionaryName getDictionaryName() {
        return DictionaryName.REPORT_TEMPLATE;
    }
}
