/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.create;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@AllArgsConstructor
@Table(name = "fr_pClientCreateF_CrtICard_in", index = "XPKfr_pClientCreateF_CrtICard_in")
@Data
public class InputDocumentCreation {

    @Column(name = "DocType")
    private String type;

    @Column(name = "RegNumber")
    private String number;

    @Column(name = "RegSeries")
    private String series;

    @Column(name = "RegDepartment")
    private String issuanceUnit;

    @Column(name = "CodePasspDepartment")
    private String issuanceUnitCode;

    @Column(name = "DateOfIssue")
    private LocalDate issuanceDate;

    @Column(name = "DateRegEnd")
    private LocalDate validUntilDate;
}
