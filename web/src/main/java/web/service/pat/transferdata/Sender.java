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

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Sender {

    private String lastname;

    private String firstname;

    private String patronymic;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthDate;

    private String citizenship;

    private String mobilePhone;

    private Document document;

    private Address address;

    private Boolean nonRus;
}
