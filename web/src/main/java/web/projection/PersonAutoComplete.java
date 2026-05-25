/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.projection;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.crm.Address;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonAutoComplete implements Serializable {

    private Long documentId;

    private String documentNumber;

    private String documentSeries;

    private String documentType;

    private Long personId;

    private String lastName;

    private String firstName;

    private String patronymic;

    private LocalDate birthDate;

    private Long personAddressId;

    private Address address;
}
