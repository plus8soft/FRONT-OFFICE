/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.transfer;

import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pTransferPaymentSystems_out", index = "XPKfr_pTransferPaymentSystems_out")
@Data
public class OutputTransferPaymentSystems {

    @Column(name = "DealTransactID", converter = BigDecimalToLongConverter.class)
    private Long id;
}
