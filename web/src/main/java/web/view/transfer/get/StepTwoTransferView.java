/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.get;

import web.service.pat.SimpleReceivingTransferResponse;
import web.service.pat.payment.PayoutTransferRequestData;
import web.service.pat.payment.ReceivingConfirmData;
import web.service.pat.payment.PaymentTransferData;
import web.view.transfer.get.PayoutTransfer;

public class StepTwoTransferView extends
        AbstractStepTwoView<PayoutTransfer, PaymentTransferData, PayoutTransferRequestData, SimpleReceivingTransferResponse,
                ReceivingConfirmData> {

    @Override
    protected void handleFindResponse(PaymentTransferData sendingTransferResponse) {
        getPayoutTransfer().setAmount(sendingTransferResponse.getAmount());
        getPayoutTransfer().setCurrency(sendingTransferResponse.getTransferCurrency());
        getPayoutTransfer().setDestinationCountry(sendingTransferResponse.getDestinationCountry());
        getPayoutTransfer().setDestinationRegion(sendingTransferResponse.getDestinationRegion());
        getPayoutTransfer().setCommission(sendingTransferResponse.getCommission());
        getPayoutTransfer().setAgentCommission(sendingTransferResponse.getAgentCommission());
        getPayoutTransfer().setSender(sendingTransferResponse.getSender());
        getPayoutTransfer().setReceiver(sendingTransferResponse.getReceiver());
    }
}
