/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
public class Receiver implements Serializable {

    private String lastname;

    private String firstname;

    private String patronymic;

    private LocalDate birthDate;

    private String mobilePhone;

    private Document document;

    private Address address;
}
