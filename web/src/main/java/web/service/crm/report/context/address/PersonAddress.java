/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.address;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonAddress {

    private String type;

    private String matchType;

    private Address address;
}
