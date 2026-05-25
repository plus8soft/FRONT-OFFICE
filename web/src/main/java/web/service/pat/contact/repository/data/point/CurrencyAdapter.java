/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.contact.repository.data.point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CurrencyAdapter extends XmlAdapter<String, List<String>> {

    private static final Map<String, String> MAP_INDEX = new HashMap<String, String>() {{
        put("EUR", "978");
        put("USD", "840");
    }};

    @Override
    public List<String> unmarshal(String v) throws Exception {
        return Stream.of(v.split(";")).filter(id -> !id.isEmpty()).map(MAP_INDEX::get).collect(Collectors.toList());
    }

    @Override
    public String marshal(List<String> v) throws Exception {
        return null;
    }
}
