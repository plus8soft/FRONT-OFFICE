/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.jaxb;

import java.math.BigDecimal;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BigDecimalAdapter extends XmlAdapter<String, BigDecimal> {

    @Override
    public BigDecimal unmarshal(String stringValue) {
        return stringValue != null ? new BigDecimal(stringValue) : null;
    }

    @Override
    public String marshal(BigDecimal value) {
        return value != null ? value.toString() : null;
    }
}
