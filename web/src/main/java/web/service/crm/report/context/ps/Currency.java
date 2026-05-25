/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.ps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.entity.ce.Case;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

    private String name;

    private Case integralCase = new Case();

    private Case fractionCase = new Case();
}
