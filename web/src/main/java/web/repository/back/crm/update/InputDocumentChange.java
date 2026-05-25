/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.update;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pChangeClientF_ModifyICard2_in", index = "XPKfr_pChangeClientF_ModifyICard2_in")
@AllArgsConstructor
@Data
public class InputDocumentChange {

    @Column(name = "IdentityCardTypeBrief")
    private String type;

    @Column(name = "Series")
    private String series;

    @Column(name = "Number")
    private String number;

    @Column(name = "DateOfIssue")
    private LocalDate issuanceDate;

    @Column(name = "DateOfExpiration")
    private LocalDate expirationDate;

    @Column(name = "Department")
    private String department;

    @Column(name = "DepartmentCode")
    private String departmentCode;
}
