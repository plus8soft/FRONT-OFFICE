/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import web.entity.core.User;
import web.entity.ps.TransferOperation;

public interface TransferSendingService<O extends AbstractSendingTransferRequest, T extends AbstractSendingTransferResponse, A extends
        AbstractSendingConfirmRequest, B extends AbstractSendingCancelRequest> {

    T createTransfer(User user, O sendingTransferRequest);

    void confirmSendingTransfer(TransferOperation transferOperation, A sendingConfirmRequest);

    void cancelSendingTransfer(TransferOperation transferOperation, B sendingCancelRequest);
}
