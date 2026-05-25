/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;
import web.service.crm.report.context.address.AddressContext;
import web.service.crm.report.context.document.additional.AdditionalDocumentContext;
import web.service.crm.report.context.document.expired.ExpiredDocumentContext;
import web.service.crm.report.context.document.main.MainDocumentContext;
import web.service.crm.report.context.person.PersonContext;
import web.service.crm.report.context.product.credit.CreditContext;
import web.service.crm.report.context.product.deposit.DepositContext;
import web.service.crm.report.context.user.UserContext;
import web.service.crm.report.context.woa.PaymentContext;

@Getter
@AllArgsConstructor
public enum ContextType {
    USER(UserContext.class, "Employee"),
    CREDITS(CreditContext.class, "Credits"),
    DEPOSITS(DepositContext.class, "Deposits"),
    CLIENT_INFO(PersonContext.class, "Client Data"),
    ADDRESSES(AddressContext.class, "Addresses"),
    MAIN_DOCUMENT(MainDocumentContext.class, "Identity Document"),
    ADDITIONAL_DOCUMENTS(AdditionalDocumentContext.class, "Additional Documents"),
    EXPIRED_DOCUMENTS(ExpiredDocumentContext.class, "Expired Documents"),
    PAYMENTS(PaymentContext.class, "Payments");

    private Class<?> contextClass;

    private String description;
}
