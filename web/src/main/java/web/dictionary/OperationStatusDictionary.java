/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import java.util.function.Function;
import org.springframework.stereotype.Repository;
import web.entity.core.DictionaryName;
import web.entity.log.OperationStatus;

@Repository
public class OperationStatusDictionary extends AbstractDictionary<OperationStatus> {

    @Override
    public DictionaryName getDictionaryName() {
        return DictionaryName.OPERATION_STATUS;
    }

    @Override
    public Function<String, OperationStatus> getKeyFunction() {
        return value -> OperationStatus.values()[Integer.valueOf(value)];
    }
}
