/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata;

import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import web.jaxb.LocalDateAdapter;
import web.jaxb.StringTrimAdapter;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Document {

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String type;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String series;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String number;

    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    private String issuanceUnit;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate issuanceDate;
}
