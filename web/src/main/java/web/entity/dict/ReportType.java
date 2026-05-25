/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;
import web.service.crm.report.context.product.ProductBriefSummary;
import web.service.crm.report.context.ps.payment.PaymentTransferContext;
import web.service.crm.report.context.ps.payment.PayoutTransferContext;
import web.service.crm.report.context.slip.CreditSlipContext;
import web.service.crm.report.context.slip.DebitSlipContext;
import web.service.crm.report.context.woa.PaymentContext;

@AllArgsConstructor
public enum ReportType {
    PRODUCTS(ProductBriefSummary.class),
    PAYMENTS(PaymentContext.class),
    CREDIT_SLIP(CreditSlipContext.class),
    DEBIT_SLIP(DebitSlipContext.class),
    PAYMENT_TRANSFER_PAYOUT(PayoutTransferContext.class),
    PAYMENT_TRANSFER_PAYMENT(PaymentTransferContext.class);

    @Getter
    private Class<?> contextClass;
}
