/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.check;

import lombok.Data;
import web.repository.back.converter.IntegerToBooleanConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientCheckPassport_out", index = "XPKfr_pClientCheckPassport_out")
@Data
public class OutputPassportCheck {

    @Column(name = "Fms", converter = IntegerToBooleanConverter.class)
    private Boolean fmsInvalid;
}
