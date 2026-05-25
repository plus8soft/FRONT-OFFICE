/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.get;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.repository.back.BackException;
import web.service.ce.CurrencyExchangeService;
import web.service.pat.AbstractReceivingConfirmRequest;
import web.service.pat.AbstractReceivingTransferRequest;
import web.service.pat.AbstractReceivingTransferResponse;
import web.service.pat.AbstractSendingTransferResponse;
import web.service.pat.TransferException;
import web.service.pat.TransferReceivingService;
import web.session.UserSession;
import web.view.Message;

@Getter
@Setter
@Log4j2
public abstract class AbstractStepTwoView<B extends AbstractPayoutTransfer, T extends AbstractSendingTransferResponse, I extends
        AbstractReceivingTransferRequest, R extends AbstractReceivingTransferResponse, C extends AbstractReceivingConfirmRequest>
        implements Serializable, Message {

    private B payoutTransfer;

    private BigDecimal cashBalance;

    @Autowired
    private TransferReceivingService<T, I, R, C> transferSendingService;

    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    private UserSession userSession;

    private boolean transferFounded;

    public void init(B payoutTransfer) {
        this.payoutTransfer = payoutTransfer;
    }

    protected void handleFindResponse(T sendingTransferResponse) {
    }

    public void find() {
        try {
            T sendingTransferResponse = transferSendingService.findTransfer(payoutTransfer.getDepartmentCode(), payoutTransfer.getControlNumber());
            if (sendingTransferResponse != null) {
                handleFindResponse(sendingTransferResponse);
                cashBalance = currencyExchangeService.receiveAccountRest(userSession.getUser(), payoutTransfer.getCurrency().getId());
                transferFounded = true;
                addInfoMessage("Transfer found");
            } else {
                addErrorMessage("Transfer not found with specified parameters");
            }
        } catch (TransferException e) {
            log.error(e.getMessages(), e);
            e.getMessages().forEach(this::addErrorMessage);
        } catch (BackException e) {
            log.error(e.getMessage(), e);
            addErrorMessage(e.getMessage());
        }
    }
}
