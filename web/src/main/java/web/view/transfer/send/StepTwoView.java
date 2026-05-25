/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import static web.entity.dict.PaymentSystemName.MONEYGRAM;
import static web.entity.dict.PaymentSystemName.MONEY_TRANSFER;
import static web.entity.dict.PaymentSystemName.WESTERN_UNION;
import web.repository.dict.PaymentPointRepository;
import web.service.pat.payment.PaymentTransferService;
import web.service.pat.payment.commission.CommissionResponse;
import web.view.transfer.send.PaymentSystemFee;

@Getter
@Setter
@Log4j2
public class StepTwoView implements Serializable {

    @Autowired
    private PaymentPointRepository paymentPointRepository;

    @Autowired
    private PaymentTransferService paymentTransferService;


    private PaymentTransfer paymentTransfer;

    private List<PaymentSystemItem> paymentSystemItems;

    private PaymentSystemItem selected;

    public void init(PaymentTransfer paymentTransfer, List<PaymentSystemItem> paymentSystemItems) {
        this.paymentTransfer = paymentTransfer;
        this.paymentSystemItems = paymentSystemItems.stream().filter(PaymentSystemItem::isEnabled).peek(paymentSystemItem -> {
            try {
                switch (paymentSystemItem.getPaymentSystem().getId()) {
                    case MONEY_TRANSFER:
                    case WESTERN_UNION:
                    case MONEYGRAM:
                        paymentSystemItem.setPaymentSystemFee(getPaymentSystemFee(paymentSystemItem.getDepartmentCode()));
                        break;
                    default:
                        log.warn("Unsupported payment system for fee: {}", paymentSystemItem.getPaymentSystem().getId());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }).collect(Collectors.toList());
        selected = this.paymentSystemItems.stream().filter(paymentSystemItem -> paymentSystemItem.getPaymentSystem()
                                                                                                 .equals(this.paymentTransfer.getPaymentSystem()))
                                          .findFirst().orElse(null);
    }

    private AbstractPaymentSystemFee getPaymentSystemFee(String departmentCode) {
        CommissionResponse response = paymentTransferService
                .calculateCommission(departmentCode, paymentTransfer.getDestinationCountry().getId(), paymentTransfer.getAcceptedCurrency().getIso(),
                                     paymentTransfer.getTransferCurrency().getIso(), paymentTransfer.getAmount(),
                                     paymentTransfer.getCitizenship().getAlpha3(), paymentTransfer.getResidentCountry().getId());
        PaymentSystemFee paymentSystemFee = new PaymentSystemFee();
        if (!paymentTransfer.getAcceptedCurrency().equals(paymentTransfer.getTransferCurrency())) {
            paymentSystemFee.setConversion(BigDecimal.ONE.divide(response.getRate(), 4, BigDecimal.ROUND_HALF_EVEN));
        }
        paymentSystemFee.setPaymentSystemCommission(response.getCommission());
        paymentSystemFee.setBankCommission(response.getAgentCommission());
        paymentSystemFee.setCommission(response.getCommission().add(response.getAgentCommission()));
        paymentSystemFee.setSum(response.getAmount().add(paymentSystemFee.getCommission()));
        paymentSystemFee.setPayAmount(response.getAmount());
        paymentSystemFee.setRate(response.getRate());
        return paymentSystemFee;
    }


    public String next() {
        paymentTransfer.setPaymentSystem(selected.getPaymentSystem());
        paymentTransfer.setPaymentSystemFee(selected.getPaymentSystemFee());
        paymentTransfer.setDepartmentCode(selected.getDepartmentCode());
        return "next";
    }

    public String back() {
        paymentTransfer.setPaymentSystem(null);
        paymentTransfer.setPaymentSystemFee(null);
        paymentTransfer.setDepartmentCode(null);
        return "back";
    }
}
