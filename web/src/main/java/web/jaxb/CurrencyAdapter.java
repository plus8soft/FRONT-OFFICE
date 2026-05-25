/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Currency adapter for XML serialization/deserialization (pass-through).
 */
public class CurrencyAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String stringValue) {
        return stringValue;
    }

    @Override
    public String marshal(String value) {
        return value;
    }
}
