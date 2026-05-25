/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import web.entity.dict.Country;
import web.entity.dict.Currency;
import web.entity.ps.TransferOperation;

@Data
public abstract class AbstractSendingTransferResponse implements Serializable {

    private TransferOperation transferOperation;

    private Country destinationCountry;

    private web.entity.dict.Region destinationRegion;

    private Currency acceptedCurrency;

    private Currency transferCurrency;

    private BigDecimal amount;

    private Sender sender;

    private Receiver receiver;
}
