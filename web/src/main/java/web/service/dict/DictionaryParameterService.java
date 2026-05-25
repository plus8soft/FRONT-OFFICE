/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.UpdateResult;
import web.service.dict.rate.ExtRateService;
import web.service.pat.payment.PaymentTransferService;
import web.service.stubs.bankstub.BankService;

@Service
@Log4j2
public class DictionaryParameterService {

    @Autowired
    private BankService bankService;

    @Autowired
    private PaymentTransferService paymentTransferService;

    @Autowired
    private ExtRateService extRateService;


    public DictionaryUpdateResult update(DictionaryParameter dictionaryParameter) {
        if (dictionaryParameter == null) {
            return DictionaryUpdateResult.builder()
                    .updateResult(UpdateResult.ERROR)
                    .message("No dictionary selected")
                    .build();
        }
        switch (dictionaryParameter.getSystem()) {
            case BANKS:
                return bankService.update(dictionaryParameter.getVersion());
            case PAYMENT_TRANSFER_INFO:
                return paymentTransferService.update();
            case RATES:
                return extRateService.update();
            default:
                // COUNTRIES and other dictionaries have no automatic update; avoid null result
                return DictionaryUpdateResult.builder()
                        .updateResult(UpdateResult.ABORTED)
                        .message("No automatic update for this dictionary type")
                        .build();
        }
    }
}
