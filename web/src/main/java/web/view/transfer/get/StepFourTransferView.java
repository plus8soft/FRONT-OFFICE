/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.get;

import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.ReportType;
import web.repository.dict.CountryRepository;
import web.service.crm.report.context.ps.AbstractPayoutContext;
import web.service.crm.report.context.ps.payment.PayoutTransferContext;
import web.service.pat.Sender;
import web.service.pat.SimpleReceivingTransferResponse;
import web.service.pat.payment.PayoutTransferRequestData;
import web.service.pat.payment.ReceivingConfirmData;
import web.service.pat.payment.PaymentTransferData;
import web.view.transfer.get.PayoutTransfer;

public class StepFourTransferView extends
        AbstractStepFourView<PayoutTransfer, PaymentTransferData, PayoutTransferRequestData, SimpleReceivingTransferResponse,
                ReceivingConfirmData> {

    @Autowired
    private CountryRepository countryRepository;

    @Override
    protected PayoutTransferRequestData createPayoutTransferData() {
        PayoutTransferRequestData payoutTransferData = new PayoutTransferRequestData();
        payoutTransferData.setCountryCode(getPayoutTransfer().getDestinationCountry().getId());
        return payoutTransferData;
    }

    @Override
    protected ReceivingConfirmData buildReceivingConfirmRequest() {
        ReceivingConfirmData receivingConfirmData = new ReceivingConfirmData();
        receivingConfirmData.setDepartmentCode(getPayoutTransfer().getDepartmentCode());
        receivingConfirmData.setControlNumber(getPayoutTransfer().getControlNumber());
        receivingConfirmData.setAgentCommission(getPayoutTransfer().getAgentCommission());
        receivingConfirmData.setAmount(getPayoutTransfer().getAmount());
        receivingConfirmData.setCommission(getPayoutTransfer().getCommission());
        receivingConfirmData.setDestinationCountryCode(getPayoutTransfer().getDestinationCountry().getId());
        receivingConfirmData.setPerson(getPayoutTransfer().getPerson());
        receivingConfirmData.setReceiver(getPayoutTransfer().getReceiver());
        receivingConfirmData.setRegion(getPayoutTransfer().getDestinationRegion().getName());
        receivingConfirmData.setSender(getPayoutTransfer().getSender());
        receivingConfirmData.setWithdrawCurrencyIso(getPayoutTransfer().getCurrency().getIso());
        return receivingConfirmData;
    }

    @Override
    protected AbstractPayoutContext buildReportContext() {
        PayoutTransferContext payoutTransferContext =
                new PayoutTransferContext(getPayoutTransfer().getDestinationCountry().getName(), getPayoutTransfer().getDestinationRegion().getName(),
                                        countryRepository.findOne(getPayoutTransfer().getPerson().getCitizenship()).getName());
        Sender sender = getPayoutTransfer().getSender();
        payoutTransferContext.setSenderFirstName(sender.getFirstname());
        payoutTransferContext.setSenderLastName(sender.getLastname());
        payoutTransferContext.setSenderPatronymic(sender.getPatronymic());
        return payoutTransferContext;
    }

    @Override
    protected ReportType getReportType() {
        // Use generic payout report type if available
        return ReportType.values().length > 0 ? ReportType.values()[0] : null;
    }

    @Override
    protected String getCode() {
        return getPayoutTransfer().getControlNumber();
    }
}
