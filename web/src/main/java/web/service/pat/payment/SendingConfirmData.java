/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import web.service.pat.AbstractSendingConfirmRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class SendingConfirmData extends AbstractSendingConfirmRequest {

    private String controlNumber;
}
