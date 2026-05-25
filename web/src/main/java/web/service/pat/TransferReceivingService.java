/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import web.entity.core.User;
import web.entity.ps.TransferOperation;

public interface TransferReceivingService<T extends AbstractSendingTransferResponse, I extends AbstractReceivingTransferRequest, R extends
        AbstractReceivingTransferResponse, C extends AbstractReceivingConfirmRequest> {

    T findTransfer(String departmentCode, String code);

    R blockTransfer(User user, I receivingTransferRequest);

    void confirmReceivingTransfer(TransferOperation transferOperation, C receivingConfirmRequest);

    void cancelReceivingTransfer(TransferOperation transferOperation, String code, String departmentCode);
}
