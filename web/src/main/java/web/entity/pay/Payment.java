/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.pay;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import web.entity.log.Operation;
import web.entity.log.OperationStatus;

@Getter
@Setter
@Entity
@DiscriminatorValue("PAY")
@PrimaryKeyJoinColumn(name = "PAYMENT_OPERATIONS_ID")
@Table(name = "PAYMENT_OPERATIONS", schema = "PAY")
public class Payment extends Operation {

    @Column(name = "PAYER")
    private String payer;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "COMMISSION_AMOUNT")
    private BigDecimal commission;

    @Column(name = "VAT")
    private Integer vat;

    @Column(name = "PAYEE_ACCOUNT")
    private String account;

    @Column(name = "PAYEE_EIN")
    private String ein;

    @Column(name = "PAYEE_NAME")
    private String counteragentName;

    @Column(name = "PAYEE_ADDRESS")
    private String counteragentAddress;

    @Column(name = "PAYEE_BANK_ROUTING_NUMBER")
    private String routingNumber;

    @Column(name = "PAYEE_BANK_NAME")
    private String bankName;

    @Column(name = "PAYEE_BANK_ACCOUNT")
    private String correspondentAccount;

    @Column(name = "PURPOSE")
    private String purpose;

    @Column(name = "CASH_SYMBOL")
    private String cashSymbol;

    @Column(name = "PAY_ACTION_NAME")
    private String payActionName;

    @Column(name = "PAYER_EIN")
    private String payerEin;

    @PrePersist
    private void prePersist() {
        setStatus(OperationStatus.COMPLETED);
    }
}
