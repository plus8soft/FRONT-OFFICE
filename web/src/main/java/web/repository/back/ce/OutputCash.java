/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.ce;

import java.math.BigDecimal;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pRestAccount_out", index = "XPKfr_pRestAccount_out")
@Data
public class OutputCash {

    @Column(name = "Rest")
    private BigDecimal rest;
}
