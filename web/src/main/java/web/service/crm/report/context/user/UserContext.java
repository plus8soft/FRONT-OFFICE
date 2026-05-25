/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    private String lastname;

    private String firstname;

    private String patronymic;

    private String positionText;
}
