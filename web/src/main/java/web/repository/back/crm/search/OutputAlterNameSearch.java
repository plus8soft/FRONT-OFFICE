/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.search;

import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.converter.StringConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientSearchF_SrchAlterName_out", index = "XPKfr_pClientSearchF_SrchAlterName_out", checkError = false)
@Data
public class OutputAlterNameSearch {

    @Column(name = "InstitutionID", converter = BigDecimalToLongConverter.class)
    private Long externalId;

    @Column(name = "TypeName", converter = StringConverter.class)
    private String declansion;

    @Column(name = "Name", converter = StringConverter.class)
    private String firstname;

    @Column(name = "SurName", converter = StringConverter.class)
    private String lastname;

    @Column(name = "PatronymicName", converter = StringConverter.class)
    private String patronymic;
}
