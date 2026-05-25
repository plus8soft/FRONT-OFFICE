/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata.payout;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import web.jaxb.BigDecimalAdapter;
import web.jaxb.CurrencyAdapter;
import web.service.pat.transferdata.AbstractRequest;
import web.service.pat.transferdata.Receiver;
import web.service.pat.transferdata.Sender;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@XmlRootElement
public class Request extends AbstractRequest {

    private String number;

    private String country;

    private String region;

    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    private String withdrawCurrency;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal amount;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal commission;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal agentCommission;

    private Sender sender;

    private Receiver receiver;

    private Receiver realReceiver;
}
