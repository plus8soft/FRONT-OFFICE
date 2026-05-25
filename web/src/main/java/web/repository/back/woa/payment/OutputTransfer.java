/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.woa.payment;

import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pTransfer_out", index = "XPKfr_pTransfer_out")
@Data
public class OutputTransfer {

    @Column(name = "DealTransactID", converter = BigDecimalToLongConverter.class)
    private Long transactId;
}
