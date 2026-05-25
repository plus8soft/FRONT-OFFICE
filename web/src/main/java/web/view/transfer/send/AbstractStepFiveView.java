/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.Country;
import web.entity.ps.Recipient;
import web.repository.back.BackException;
import web.repository.crm.PersonRepository;
import web.repository.dict.CountryRepository;
import web.service.pat.AbstractSendingCancelRequest;
import web.service.pat.AbstractSendingConfirmRequest;
import web.service.pat.AbstractSendingTransferRequest;
import web.service.pat.AbstractSendingTransferResponse;
import web.service.pat.TransferException;
import web.service.pat.TransferSendingService;
import web.session.UserSession;
import web.view.Message;
import web.view.component.AddressAutoComplete;

@Getter
@Setter
@Log4j2
public abstract class AbstractStepFiveView<C extends AbstractPaymentSystemFee, O extends AbstractSendingTransferRequest, T extends
        AbstractSendingTransferResponse, A extends AbstractSendingConfirmRequest, B extends AbstractSendingCancelRequest>
        implements Serializable, Message {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private TransferSendingService<O, T, A, B> transferSendingService;

    @Autowired
    private UserSession userSession;

    private Recipient recipient;

    private PaymentTransfer<C, T> paymentTransfer;

    private List<Country> countries;

    public void init(PaymentTransfer<C, T> paymentTransfer, Long clientId) {
        this.paymentTransfer = paymentTransfer;
        paymentTransfer.setSender(personRepository.findOne(clientId));
        recipient = paymentTransfer.getRecipient() != null ? paymentTransfer.getRecipient() : new Recipient();
        countries = countryRepository.findAllByOrderByNameAsc();
    }

    protected abstract O buildSendingTransferRequest();

    protected void handleSendingTransferResponse(T sendingTransferResponse) {
    }

    public String next() {
        try {
            recipient.setUser(userSession.getUser());
            paymentTransfer.setRecipient(recipient);
            O sendingTransferRequest = buildSendingTransferRequest();
            sendingTransferRequest.setDepartmentCode(paymentTransfer.getDepartmentCode());
            sendingTransferRequest.setPerson(paymentTransfer.getSender());
            sendingTransferRequest.setAcceptedCurrency(paymentTransfer.getAcceptedCurrency());
            sendingTransferRequest.setAmount(paymentTransfer.getAmount());
            sendingTransferRequest.setDestinationCountry(paymentTransfer.getDestinationCountry());
            sendingTransferRequest.setDestinationRegion(paymentTransfer.getDestinationRegion());
            sendingTransferRequest.setPaymentSystem(paymentTransfer.getPaymentSystem());
            sendingTransferRequest.setRecipient(paymentTransfer.getRecipient());
            sendingTransferRequest.setTransferCurrency(paymentTransfer.getTransferCurrency());
            sendingTransferRequest.setCommission(paymentTransfer.getPaymentSystemFee().getPaymentSystemCommission());
            sendingTransferRequest.setAgentCommission(paymentTransfer.getPaymentSystemFee().getBankCommission());
            T sendingTransferResponse = transferSendingService.createTransfer(userSession.getUser(), sendingTransferRequest);
            paymentTransfer.setTransferOperation(sendingTransferResponse.getTransferOperation());
            paymentTransfer.setTransferData(sendingTransferResponse);
            handleSendingTransferResponse(sendingTransferResponse);
            return "next";
        } catch (BackException e) {
            addErrorMessage(e.getMessage());
            log.error(e.getMessage(), e);
        } catch (TransferException e) {
            log.error(e.getMessages(), e);
            e.getMessages().forEach(this::addErrorMessage);
        } catch (Exception e) {
            addErrorMessage("Error creating transfer");
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
