/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.search;

import java.time.LocalDate;
import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.converter.StringConverter;
import web.repository.back.converter.TimestampToLocalDateConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientSearchF_SrchICard_out", index = "XPKfr_pClientSearchF_SrchICard_out", checkError = false)
@Data
public class OutputDocumentSearch {

    @Column(name = "InstitutionID", converter = BigDecimalToLongConverter.class)
    private Long externalId;

    @Column(name = "DocType", converter = StringConverter.class)
    private String type;

    @Column(name = "RegSeries", converter = StringConverter.class)
    private String series;

    @Column(name = "RegNumber", converter = StringConverter.class)
    private String number;

    @Column(name = "CodePasspDepartment", converter = StringConverter.class)
    private String issuanceUnitCode;

    @Column(name = "RegDepartment", converter = StringConverter.class)
    private String issuanceUnit;

    @Column(name = "DateOfIssue", converter = TimestampToLocalDateConverter.class)
    private LocalDate issuanceDate;

    @Column(name = "DateRegEnd", converter = TimestampToLocalDateConverter.class)
    private LocalDate validUntilDate;
}
