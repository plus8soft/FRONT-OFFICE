/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import web.service.pat.payment.PaymentTransferRequestData;
import web.service.pat.payment.SendingCancelData;
import web.service.pat.payment.SendingConfirmData;
import web.service.pat.payment.PaymentTransferData;
import web.view.transfer.send.PaymentSystemFee;

public class StepFiveTransferView extends
        AbstractStepFiveView<PaymentSystemFee, PaymentTransferRequestData, PaymentTransferData, SendingConfirmData,
                SendingCancelData> {

    @Override
    protected PaymentTransferRequestData buildSendingTransferRequest() {
        PaymentTransferRequestData transferData = new PaymentTransferRequestData();
        transferData.setPayAmount(getPaymentTransfer().getPaymentSystemFee().getPayAmount());
        transferData.setRate(getPaymentTransfer().getPaymentSystemFee().getRate());
        return transferData;
    }
}
