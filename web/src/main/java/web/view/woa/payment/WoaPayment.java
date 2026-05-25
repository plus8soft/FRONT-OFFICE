/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.woa.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import web.entity.crm.Person;
import web.entity.dict.Account;
import web.entity.dict.Bank;
import web.entity.dict.Counteragent;
import web.entity.dict.CounteragentPayAction;
import web.entity.log.OperationCode;

@Data
public class WoaPayment implements Serializable {

    private Counteragent counteragent;

    private Bank bank;

    private Person person;

    private OperationCode paymentOperationCode;

    private CounteragentPayAction payAction;

    private Account account;

    private Integer vat;

    private String purpose;

    private BigDecimal sum;

    private BigDecimal counteragentCommission;

    private BigDecimal vatSum;

    private BigDecimal total;

    private String senderEin;

    private LocalDate docDate;
}
