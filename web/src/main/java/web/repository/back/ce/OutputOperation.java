/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.ce;

import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pCurrencyExchange_out", index = "XPKfr_pCurrencyExchange_out")
@Data
public class OutputOperation {

    @Column(name = "DealTransactID", converter = BigDecimalToLongConverter.class)
    private Long id;
}
