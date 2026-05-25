/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.check;

import lombok.Data;
import web.repository.back.converter.ShortToBooleanConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Data
@Table(name = "fr_pClientCheckTerror_out", index = "XPKfr_pClientCheckTerror_out")
public class OutputTerroristCheck {

    @Column(name = "UnreliableFlag", converter = ShortToBooleanConverter.class)
    private Boolean terrorist;
}
