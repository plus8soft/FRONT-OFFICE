/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.jaxb;

import java.util.Optional;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringTrimAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String source) throws Exception {
        return Optional.ofNullable(source).map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
    }

    @Override
    public String marshal(String value) throws Exception {
        return value;
    }
}
