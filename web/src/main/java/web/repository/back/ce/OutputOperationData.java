/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.ce;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;
import web.repository.back.converter.StringConverter;
import web.repository.back.converter.StringToLongConverter;
import web.repository.back.converter.TimestampToLocalDateConverter;
import web.repository.back.converter.TimestampToLocalTimeConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

/**
 * Maps the result set of the operations report stored procedure.
 * Expected column names when recreating the backend SP:
 * DocNumber, TransactionCode, PaymentPurpose, PayerName, PayerAddress, PayerBank,
 * PayerRoutingNumber, PayerAccountInfo, ReceiverName, ReceiverAddress, ReceiverBank,
 * ReceiverRoutingNumber, QualifyProblems, OperationDate, OperationTime.
 */
@Table(name = "fr_pReportMessageSdel_out", index = "XPKfr_pReportMessageSdel_out")
@Data
public class OutputOperationData {

    @Column(name = "DocNumber", converter = StringToLongConverter.class)
    private Long number;

    @Column(name = "TransactionCode", converter = StringConverter.class)
    private String requireTransactionCode;

    @Column(name = "PaymentPurpose", converter = StringConverter.class)
    private String paymentPurpose;

    @Column(name = "PayerName", converter = StringConverter.class)
    private String payerName;

    @Column(name = "PayerAddress", converter = StringConverter.class)
    private String payerAddressOrDocument;

    @Column(name = "PayerBank", converter = StringConverter.class)
    private String payerCreditOrganization;

    @Column(name = "PayerRoutingNumber", converter = StringConverter.class)
    private String payerRoutingNumber;

    @Column(name = "PayerAccountInfo", converter = StringConverter.class)
    private String payerAccountInfo;

    @Column(name = "ReceiverName", converter = StringConverter.class)
    private String receiverName;

    @Column(name = "ReceiverAddress", converter = StringConverter.class)
    private String receiverAddressOrDocument;

    @Column(name = "ReceiverBank", converter = StringConverter.class)
    private String receiverCreditOrganization;

    @Column(name = "ReceiverRoutingNumber", converter = StringConverter.class)
    private String receiverRoutingNumber;

    @Column(name = "QualifyProblems", converter = StringConverter.class)
    private String qualifyProblems;

    @Column(name = "OperationDate", converter = TimestampToLocalDateConverter.class)
    private LocalDate date;

    @Column(name = "OperationTime", converter = TimestampToLocalTimeConverter.class)
    private LocalTime time;
}
