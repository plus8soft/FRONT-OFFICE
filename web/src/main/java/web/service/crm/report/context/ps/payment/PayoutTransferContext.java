/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.ps.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import web.service.crm.report.context.ps.AbstractPayoutContext;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayoutTransferContext extends AbstractPayoutContext {

    private String destinationCountry;

    private String destinationRegion;

    private String receiverCitizenship;
}
