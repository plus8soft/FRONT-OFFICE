/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.create;

import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.converter.StringConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientCreateF_Crt_out", index = "XPKfr_pClientCreateF_Crt_out")
@Data
public class OutputCreation {

    @Column(name = "InstitutionID", converter = BigDecimalToLongConverter.class)
    private Long externalId;

    @Column(name = "DocSeries", converter = StringConverter.class)
    private String documentSeries;

    @Column(name = "DocNumber", converter = StringConverter.class)
    private String documentNumber;
}
