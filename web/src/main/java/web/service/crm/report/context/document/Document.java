/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.document;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    private String type;

    private String series;

    private String number;

    private String issuanceUnit;

    private String issuanceUnitCode;

    private LocalDate issuanceDate;

    private LocalDate validUntilDate;
}
