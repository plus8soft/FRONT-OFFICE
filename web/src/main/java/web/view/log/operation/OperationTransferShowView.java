/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.log.operation;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import web.entity.ps.TransferOperation;

@Getter
@Setter
public class OperationTransferShowView implements Serializable {

    private TransferOperation operation;

    public void init(TransferOperation operation) {
        this.operation = operation;
    }
}
