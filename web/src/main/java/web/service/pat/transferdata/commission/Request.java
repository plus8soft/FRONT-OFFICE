/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata.commission;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import web.jaxb.BigDecimalAdapter;
import web.jaxb.CurrencyAdapter;
import web.service.pat.transferdata.AbstractRequest;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@XmlRootElement
public class Request extends AbstractRequest {

    private String country;

    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    private String acceptedCurrency;

    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    private String withdrawCurrency;

    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal amount;

    private String citizenshipCountry;

    private Boolean nonRus;

    public Request(String departmentCode, String country, String acceptedCurrency, String withdrawCurrency, BigDecimal amount,
                   String citizenshipCountry, Boolean nonRus) {
        setId(departmentCode);
        this.country = country;
        this.acceptedCurrency = acceptedCurrency;
        this.withdrawCurrency = withdrawCurrency;
        this.amount = amount;
        this.citizenshipCountry = citizenshipCountry;
        this.nonRus = nonRus;
    }
}
