/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import java.util.function.Function;
import org.springframework.stereotype.Repository;
import web.entity.core.DictionaryName;
import web.entity.log.OperationCode;

@Repository
public class OperationCodeDictionary extends AbstractDictionary<OperationCode> {

    @Override
    public DictionaryName getDictionaryName() {
        return DictionaryName.OPERATION_CODE;
    }

    @Override
    public Function<String, OperationCode> getKeyFunction() {
        return value -> OperationCode.byCode(Integer.valueOf(value));
    }
}
