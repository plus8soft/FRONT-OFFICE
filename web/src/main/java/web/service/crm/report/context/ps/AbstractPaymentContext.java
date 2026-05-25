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
public abstract class AbstractPaymentContext {

    private String destinationCountry;

    private String destinationRegion;

    private String transferNumber;

    private LocalDate transferDate;

    private BigDecimal amount;

    private BigDecimal acceptedAmount;

    private BigDecimal issuanceAmount;

    private Currency acceptedCurrency;

    private Currency issuanceCurrency;

    private BigDecimal commission;

    private String additionalInfo;

    private Document document;

    private String receiverFirstName;

    private String receiverLastName;

    private String receiverPatronymic;

    private String senderLastName;

    private String senderFirstName;

    private String senderPatronymic;

    private LocalDate senderBirthDate;

    private Address senderAddress;

    private String senderPaymentPoint;

    private String senderPhone;
}
