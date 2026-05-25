/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.ps;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.service.crm.report.context.address.Address;
import web.service.crm.report.context.document.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractPayoutContext {

    private String transferNumber;

    private LocalDate issuanceDate;

    private BigDecimal amount;

    private Currency currency;

    private String additionalInfo;

    private String senderLastName;

    private String senderFirstName;

    private String senderPatronymic;

    private String receiverPaymentPoint;

    private String phone;

    private Address address;

    private Document document;

    private String receiverFirstName;

    private String receiverLastName;

    private String receiverPatronymic;

    private LocalDate receiverBirthDate;
}
