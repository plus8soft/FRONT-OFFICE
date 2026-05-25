/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.search;

import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.converter.StringConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientSearchF_SrchContact_out", index = "XPKfr_pClientSearchF_SrchContact_out", checkError = false)
@Data
public class OutputContactSearch {

    @Column(name = "InstitutionID", converter = BigDecimalToLongConverter.class)
    private Long externalId;

    @Column(name = "ContactType", converter = StringConverter.class)
    private String type;

    @Column(name = "ContactValue", converter = StringConverter.class)
    private String data;
}
