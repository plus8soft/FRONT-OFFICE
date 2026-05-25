/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import web.service.pat.AbstractSendingCancelRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class SendingCancelData extends AbstractSendingCancelRequest {

    private String controlNumber;
}
