/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict.scheduling;

import java.util.concurrent.Callable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import web.entity.dict.DictionaryParameter;
import web.service.dict.DictionaryParameterService;
import web.service.dict.DictionaryUpdateResult;

@Log4j2
public class DictionaryCallable implements Callable<DictionaryUpdateResult> {

    private DictionaryParameterService dictionaryParameterService;

    @Getter(AccessLevel.PACKAGE)
    private DictionaryParameter dictionaryParameter;

    public DictionaryCallable(DictionaryParameterService dictionaryParameterService, DictionaryParameter dictionaryParameter) {
        this.dictionaryParameterService = dictionaryParameterService;
        this.dictionaryParameter = dictionaryParameter;
    }

    @Override
    public DictionaryUpdateResult call() throws Exception {
        return dictionaryParameterService.update(dictionaryParameter);
    }
}
