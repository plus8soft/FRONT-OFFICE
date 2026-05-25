/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.get;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.entity.dict.Country;
import web.entity.dict.Region;
import web.service.pat.Receiver;
import web.service.pat.Sender;

@Data
@EqualsAndHashCode(callSuper = true)
public class PayoutTransfer extends AbstractPayoutTransfer {

    private Country destinationCountry;

    private Region destinationRegion;

    private Sender sender;

    private Receiver receiver;

    private BigDecimal commission;

    private BigDecimal agentCommission;
}
