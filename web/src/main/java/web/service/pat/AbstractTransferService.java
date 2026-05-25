/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.User;
import web.entity.crm.Contact;
import web.entity.crm.Person;
import web.entity.crm.PersonAddress_;
import web.entity.dict.Currency;
import web.entity.dict.PaymentSystem;
import web.entity.log.AddressHistory;
import web.entity.log.OperationCode;
import web.entity.log.OperationStatus;
import web.entity.log.PersonHistory;
import web.entity.ps.TransferOperation;
import web.repository.crm.DocumentRepository;
import web.repository.crm.PersonAddressRepository;
import web.repository.log.PersonHistoryRepository;
import web.repository.ps.RecipientRepository;
import web.repository.ps.TransferOperationRepository;
import web.service.back.DirectionType;
import web.service.back.RecipientType;
import web.service.back.TransferBackService;
import web.service.back.TransferType;
import web.service.crm.ContactService;
import web.utils.Addresses;
import web.utils.Contacts;

@Service
public abstract class AbstractTransferService<O extends AbstractSendingTransferRequest, T extends AbstractSendingTransferResponse, A extends
        AbstractSendingConfirmRequest, B extends AbstractSendingCancelRequest, I extends AbstractReceivingTransferRequest, R extends
        AbstractReceivingTransferResponse, C extends AbstractReceivingConfirmRequest>
        implements TransferSendingService<O, T, A, B>, TransferReceivingService<T, I, R, C> {

    @Autowired
    private TransferOperationRepository transferOperationRepository;

    @Autowired
    private PersonHistoryRepository personHistoryRepository;

    @Autowired
    private TransferBackService transferBackService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PersonAddressRepository personAddressRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private ContactService contactService;

    private PersonHistory getPersonHistory(Person person) {
        PersonHistory personHistory = new PersonHistory();
        personHistory.setPerson(person);
        personHistory.setDocument(documentRepository.findMain(person));
        personHistory.setAddress(new AddressHistory(personAddressRepository.findOne((root, query, cb) -> {
            root.fetch(PersonAddress_.address);
            return cb.and(cb.equal(root.get(PersonAddress_.person), person), cb.equal(root.get(PersonAddress_.type), Addresses.STAYING_TYPE));
        })));
        personHistory.setHomePhone(
                Optional.ofNullable(contactService.findMainOrAnyOtherContact(person, Contacts.HOME_PHONE_TYPE)).map(Contact::getData).orElse(null));
        personHistory.setMobilePhone(contactService.findMainOrAnyOtherContact(person, Contacts.MOBILE_PHONE_TYPE).getData());
        personHistory.setEmail(
                Optional.ofNullable(contactService.findMainOrAnyOtherContact(person, Contacts.EMAIL_TYPE)).map(Contact::getData).orElse(null));
        return personHistory;
    }

    private TransferOperation buildTransferOperation(User user, PaymentSystem paymentSystem, Person person, Currency currency, BigDecimal amount,
                                                     OperationCode operationCode, String number) {
        TransferOperation transferOperation = new TransferOperation();
        transferOperation.setUser(user);
        transferOperation.setDepartment(user.getDepartment());
        transferOperation.setPaymentSystem(paymentSystem);
        transferOperation.setPersonHistory(getPersonHistory(person));
        transferOperation.setCurrency(currency);
        transferOperation.setAmount(amount);
        transferOperation.setDate(LocalDateTime.now(user.getDepartment().getZoneId()));
        transferOperation.setCode(operationCode);
        transferOperation.setStatus(OperationStatus.PERFORMED);
        transferOperation.setNumber(number);
        return transferOperation;
    }

    protected abstract String getPaymentPointCode(O outTransfer);

    protected TransferOperation createTransferOperation(User user, O outTransfer, BigDecimal amount, String number, BigDecimal bankCommission,
                                                        BigDecimal systemCommission) {
        TransferOperation transferOperation =
                buildTransferOperation(user, outTransfer.getPaymentSystem(), outTransfer.getPerson(), outTransfer.getAcceptedCurrency(), amount,
                                       OperationCode.SEND, number);
        transferOperation.setPoint(getPaymentPointCode(outTransfer));
        transferOperation.setRecipient(outTransfer.getRecipient());
        transferOperation.setTransferCurrency(outTransfer.getTransferCurrency());
        transferOperation.setTransferAmount(outTransfer.getAmount());
        transferOperation.setCountry(outTransfer.getDestinationCountry().getAlpha3());
        transferOperation.setRegion(outTransfer.getDestinationRegion().getName());
        transferOperation.setBankCommissionCurrency(outTransfer.getAcceptedCurrency());
        transferOperation.setSystemCommissionCurrency(outTransfer.getAcceptedCurrency());
        transferOperation.setBankCommissionAmount(bankCommission);
        transferOperation.setSystemCommissionAmount(systemCommission);
        return transferOperation;
    }

    protected abstract T create(User user, O sendingTransferRequest);

    protected abstract String getBackPaymentSystemName();

    @Override
    @Transactional
    public T createTransfer(User user, O sendingTransferRequest) {
        T sendingTransferResponse = create(user, sendingTransferRequest);
        recipientRepository.save(sendingTransferRequest.getRecipient());
        TransferOperation transferOperation = sendingTransferResponse.getTransferOperation();
        transferOperation.setExternalId(transferBackService.processTransfer(user.getLogin(), sendingTransferRequest.getPerson().getExternalId(),
                                                                            user.getDepartment().getExternalId(), transferOperation.getDate(),
                                                                            transferOperation.getAmount(),
                                                                            transferOperation.getBankCommissionAmount(),
                                                                            transferOperation.getSystemCommissionAmount(),
                                                                            sendingTransferRequest.getTransferCurrency().getId(),
                                                                            getBackPaymentSystemName(), null, TransferType.RECEPTION,
                                                                            sendingTransferRequest.getDestinationCountry().getId()
                                                                                                  .equals(Addresses.COUNTRY_RU) ?
                                                                            DirectionType.NATIONAL : DirectionType.INTERNATIONAL,
                                                                            sendingTransferRequest.getPerson().getResidentCountry()
                                                                                                  .equals(Addresses.COUNTRY_RU) ?
                                                                            RecipientType.RESIDENT : RecipientType.NON_RESIDENT));
        transferOperationRepository.save(transferOperation);
        transferOperation.getPersonHistory().setOperation(transferOperation);
        personHistoryRepository.save(transferOperation.getPersonHistory());
        return sendingTransferResponse;
    }

    protected abstract R block(User user, I payoutTransferData);

    protected abstract DirectionType getPayoutDirectionType(I payoutTransferData, R receivingTransferResponse);

    @Override
    @Transactional
    public R blockTransfer(User user, I payoutTransferData) {
        R receivingTransferResponse = block(user, payoutTransferData);
        Person person = payoutTransferData.getPerson();
        TransferOperation transferOperation =
                buildTransferOperation(user, payoutTransferData.getPaymentSystem(), person, payoutTransferData.getCurrency(),
                                       payoutTransferData.getAmount(), OperationCode.ISSUE, payoutTransferData.getControlNumber());
        transferOperation.setExternalId(transferBackService
                                                .processTransfer(user.getLogin(), person.getExternalId(), user.getDepartment().getExternalId(),
                                                                 transferOperation.getDate(), payoutTransferData.getAmount(), null, null,
                                                                 payoutTransferData.getCurrency().getId(), getBackPaymentSystemName(), null,
                                                                 TransferType.ISSUE,
                                                                 getPayoutDirectionType(payoutTransferData, receivingTransferResponse),
                                                                 person.getResidentCountry().equals(Addresses.COUNTRY_RU) ?
                                                                 RecipientType.RESIDENT : RecipientType.NON_RESIDENT));
        PersonHistory personHistory = transferOperation.getPersonHistory();
        transferOperation.setPersonHistory(null);
        transferOperationRepository.save(transferOperation);
        transferOperation.setPersonHistory(personHistory);
        personHistory.setOperation(transferOperation);
        personHistoryRepository.save(personHistory);
        receivingTransferResponse.setTransferOperation(transferOperation);
        return receivingTransferResponse;
    }

    protected abstract void cancelReceiving(String code, String departmentCode);

    @Override
    @Transactional
    public void cancelReceivingTransfer(TransferOperation transferOperation, String code, String departmentCode) {
        cancelReceiving(code, departmentCode);
        save(transferOperation, OperationStatus.CANCELED);
    }

    private void save(TransferOperation transferOperation, OperationStatus operationStatus) {
        transferOperation.setStatus(operationStatus);
        transferOperationRepository.save(transferOperation);
    }

    protected abstract void confirmReceiving(C receivingConfirmRequest);

    @Override
    public void confirmReceivingTransfer(TransferOperation transferOperation, C receivingConfirmRequest) {
        confirmReceiving(receivingConfirmRequest);
        save(transferOperation, OperationStatus.COMPLETED);
    }

    @Override
    public void cancelSendingTransfer(TransferOperation transferOperation, B sendingCancelRequest) {
        cancelSending(sendingCancelRequest);
        save(transferOperation, OperationStatus.CANCELED);
    }

    protected abstract void cancelSending(B sendingCancelRequest);

    @Override
    public void confirmSendingTransfer(TransferOperation transferOperation, A sendingConfirmRequest) {
        confirmSending(sendingConfirmRequest);
        save(transferOperation, OperationStatus.COMPLETED);
    }

    protected abstract void confirmSending(A sendingConfirmRequest);
}
