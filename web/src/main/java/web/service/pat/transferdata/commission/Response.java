/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata.commission;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.jaxb.BigDecimalAdapter;
import web.service.pat.transferdata.AbstractResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlJavaTypeAdapter(BigDecimalAdapter.class)
@XmlRootElement
public class Response extends AbstractResponse {

    private BigDecimal amount;

    private BigDecimal commission;

    private BigDecimal agentCommission;

    private BigDecimal rate;
}
