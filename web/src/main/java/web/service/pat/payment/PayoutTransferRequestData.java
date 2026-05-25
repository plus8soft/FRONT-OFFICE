/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import web.service.pat.AbstractReceivingTransferRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class PayoutTransferRequestData extends AbstractReceivingTransferRequest {

    private String countryCode;
}
