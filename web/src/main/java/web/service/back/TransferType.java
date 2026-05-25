/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import lombok.Getter;

@Getter
public enum TransferType {
    RECEPTION,
    ISSUE,
    RETURN_BY_BANK,
    RETURN_BY_CUSTOMER
}
