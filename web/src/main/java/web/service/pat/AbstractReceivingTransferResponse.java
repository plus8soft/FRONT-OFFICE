/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import lombok.Data;
import web.entity.ps.TransferOperation;

@Data
public abstract class AbstractReceivingTransferResponse {

    private TransferOperation transferOperation;
}
