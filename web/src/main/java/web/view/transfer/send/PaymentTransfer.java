/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.crm.Person;
import web.entity.dict.Country;
import web.entity.dict.Currency;
import web.entity.dict.PaymentPoint;
import web.entity.dict.PaymentSystem;
import web.entity.dict.Region;
import web.entity.ps.Recipient;
import web.entity.ps.TransferOperation;
import web.service.pat.AbstractSendingTransferResponse;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentTransfer<T extends AbstractPaymentSystemFee, E extends AbstractSendingTransferResponse> implements Serializable {

    private String id;

    private String signDocument;

    private Country destinationCountry;

    private Region destinationRegion;

    private Currency acceptedCurrency;

    private Currency transferCurrency;

    private BigDecimal amount;

    private Country citizenship;

    private Country residentCountry;

    private PaymentSystem paymentSystem;

    private String departmentCode;

    private boolean addressed = true;

    private PaymentPoint paymentPoint;

    private Recipient recipient;

    private Person sender;

    private T paymentSystemFee;

    private TransferOperation transferOperation;

    private E transferData;
}
