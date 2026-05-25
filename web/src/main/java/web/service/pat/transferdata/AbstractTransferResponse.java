/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata;

import java.math.BigDecimal;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.jaxb.BigDecimalAdapter;
import web.jaxb.CurrencyAdapter;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractTransferResponse extends AbstractResponse {

    private String controlNumber;

    private String country;

    private String region;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal amount;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal rate;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal payAmount;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal commission;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal agentCommission;

    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    private String acceptedCurrency;

    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    private String withdrawCurrency;

    private Sender sender;

    private Receiver receiver;
}
