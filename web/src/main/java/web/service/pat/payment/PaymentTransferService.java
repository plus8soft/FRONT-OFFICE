/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.User;
import web.entity.dict.Country;
import web.entity.dict.Currency;
import web.entity.dict.Currency_;
import web.entity.dict.PaymentPoint;
import web.entity.dict.PaymentSystem;
import web.entity.dict.PaymentSystemName;
import web.entity.dict.Region;
import web.entity.dict.Region_;
import web.entity.dict.UpdateResult;
import web.entity.ps.TransferOperation;
import web.repository.dict.CountryRepository;
import web.repository.dict.CurrencyRepository;
import web.repository.dict.PaymentPointRepository;
import web.repository.dict.PaymentSystemRepository;
import web.repository.dict.RegionRepository;
import web.service.back.DirectionType;
import web.service.dict.DictionaryUpdateResult;
import web.service.pat.AbstractTransferService;
import web.service.pat.transferdata.AbstractTransferResponse;
import web.service.pat.ExtendedReceiver;
import web.service.pat.ExtendedSender;
import web.service.pat.Pose;
import web.service.pat.SimpleReceivingTransferResponse;
import web.service.pat.payment.commission.CommissionResponse;
import web.utils.Addresses;
import java.math.RoundingMode;

@Service
public class PaymentTransferService extends
        AbstractTransferService<PaymentTransferRequestData, PaymentTransferData, SendingConfirmData, SendingCancelData,
                PayoutTransferRequestData, SimpleReceivingTransferResponse, ReceivingConfirmData> {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private PaymentSystemRepository paymentSystemRepository;

    @Autowired
    private PaymentPointRepository paymentPointRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    /**
     * Stub implementation: returns test payment points and regions.
     * This replaces the previous payment system API integration.
     * 
     * Purpose:
     * - Provides a structure for integrating international payment systems
     * - Returns hardcoded test data for demonstration
     * - Currently returns test payment points - you can implement your own API integration
     * 
     * Future integration options:
     * - Western Union API
     * - MoneyGram API
     * - Ria Money Transfer API
     * - Remitly API
     * - Wise (TransferWise) API
     * - Xoom API
     */
    @Transactional
    public DictionaryUpdateResult update() {
        // Stub: returns test payment points instead of calling real API
        // Currently returns test data - you can implement your own API for payment system integration
        // In real implementation, this should:
        // 1. Make HTTP request to payment system API
        // 2. Parse the response (JSON/XML)
        // 3. Map response to Pose entities
        // 4. Update payment points and regions in database
        // 5. Return update result with status
        
        List<Pose> testPoses = createTestPoses();
        updatePaymentPoints(testPoses, updateRegions(testPoses));
        DictionaryUpdateResult dictionaryUpdateResult = new DictionaryUpdateResult();
        dictionaryUpdateResult.setUpdateDate(LocalDateTime.now());
        dictionaryUpdateResult.setUpdateResult(UpdateResult.SUCCESSFULLY);
        return dictionaryUpdateResult;
    }
    
    /**
     * Creates test payment points for demonstration.
     */
    private List<Pose> createTestPoses() {
        List<Pose> poses = new ArrayList<>();
        
        // Test payment points in USA
        poses.add(Pose.builder()
                .id("US-NY-001")
                .name("New York Main Office")
                .region(new web.service.pat.Region("US", "New York"))
                .address("123 Main Street, New York, NY 10001")
                .currencies(Arrays.asList("USD", "EUR"))
                .build());
        
        poses.add(Pose.builder()
                .id("US-CA-001")
                .name("Los Angeles Branch")
                .region(new web.service.pat.Region("US", "California"))
                .address("456 Broadway, Los Angeles, CA 90001")
                .currencies(Arrays.asList("USD", "EUR"))
                .build());
        
        // Test payment points in UK
        poses.add(Pose.builder()
                .id("GB-LON-001")
                .name("London Central")
                .region(new web.service.pat.Region("GB", "London"))
                .address("789 Oxford Street, London, W1D 2HX")
                .currencies(Arrays.asList("GBP", "EUR", "USD"))
                .build());
        
        return poses;
    }

    private Map<web.service.pat.Region, Region> updateRegions(List<Pose> poses) {
        Map<web.service.pat.Region, Region> currentRegions = regionRepository.findAll((root, query, cb) -> {
            root.fetch(Region_.country);
            return null;
        }).parallelStream().collect(Collectors
                                            .toMap(region -> new web.service.pat.Region(region.getCountry().getId(), region.getName().toUpperCase()),
                                                   Function.identity()));
        Map<String, Country> countryMap = countryRepository.findAll().stream().collect(Collectors.toMap(Country::getId, Function.identity()));
        List<Region> regions = poses.parallelStream().map(Pose::getRegion).sequential().collect(Collectors.toMap(region -> new web.service.pat.Region(
                region.getCountryCode(), region.getName().toUpperCase()), Function.identity(), (oldRegion, newRegion) -> oldRegion)).values().stream()
                                    .filter(region -> !currentRegions
                                            .containsKey(new web.service.pat.Region(region.getCountryCode(), region.getName().toUpperCase())))
                                    .peek(region -> {
                                        if (!countryMap.containsKey(region.getCountryCode())) {
                                            throw new RuntimeException("Country not found in directory - " + region.getCountryCode());
                                        }
                                    }).map(paymentRegion -> {
                    Region region = new Region();
                    region.setCountry(countryMap.get(paymentRegion.getCountryCode()));
                    region.setName(paymentRegion.getName());
                    region.setEnabled(true);
                    return region;
                }).collect(Collectors.toList());
        currentRegions.putAll(regionRepository.save(regions).stream().collect(Collectors.toMap(region -> new web.service.pat.Region(
                region.getCountry().getId(), region.getName().toUpperCase()), Function.identity())));
        return currentRegions;
    }

    private void updatePaymentPoints(List<Pose> poses, Map<web.service.pat.Region, Region> regionMap) {
        Map<String, Currency> currencyMap = currencyRepository.findAll((root, query, cb) -> root.get(Currency_.iso).in("USD", "EUR")).stream()
                                                              .collect(Collectors.toMap(Currency::getIso, Function.identity()));
        poses.stream().filter(pose -> pose.getCurrencies().stream().anyMatch(currencyIso -> !currencyMap.containsKey(currencyIso))).findFirst()
             .ifPresent(pose -> {
                 throw new RuntimeException("Currency(ies) not found: " + String.join(", ", pose.getCurrencies().stream()
                                                                                             .filter(currencyIso -> !currencyMap
                                                                                                     .containsKey(currencyIso))
                                                                                             .collect(Collectors.toList())));
             });
        // Use first available payment system or create a generic one
        PaymentSystem paymentSystem = paymentSystemRepository.findAll().stream()
                .findFirst()
                .orElse(null);
        if (paymentSystem == null) {
            // If no payment system exists, skip payment point update
            return;
        }
        paymentPointRepository.deleteByPaymentSystem(paymentSystem);
        paymentPointRepository.save(poses.parallelStream().map(pose -> {
            web.service.pat.Region paymentRegion = pose.getRegion();
            Region region = regionMap.get(new web.service.pat.Region(paymentRegion.getCountryCode(), paymentRegion.getName().toUpperCase()));
            PaymentPoint paymentPoint = new PaymentPoint();
            paymentPoint.setName(pose.getName());
            paymentPoint.setCode(pose.getId());
            paymentPoint.setAddress(pose.getAddress());
            paymentPoint.setCountry(region.getCountry());
            paymentPoint.setRegion(region);
            paymentPoint.setPaymentSystem(paymentSystem);
            paymentPoint.setCurrencies(pose.getCurrencies().stream().map(currencyMap::get).collect(Collectors.toSet()));
            return paymentPoint;
        }).collect(Collectors.toList()));
    }

    /**
     * Stub implementation: returns test commission data.
     * This replaces the previous payment system API integration.
     */
    public CommissionResponse calculateCommission(String departmentCode, String countryIso, String acceptedCurrencyIso, String withdrawCurrencyIso,
                                        BigDecimal amount, String citizenshipCountryCode, String residentCountryIso) {
        // Stub: returns test commission instead of calling real API
        // Currently returns test data - you can implement your own API for commission calculation
        CommissionResponse response = new CommissionResponse();
        response.setAmount(amount);
        // Test commission: 2% of amount
        response.setCommission(amount.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP));
        // Test agent commission: 1% of amount
        response.setAgentCommission(amount.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP));
        // Test rate: 1.0 for same currency, 0.85 for USD to EUR, 1.18 for EUR to USD
        if (acceptedCurrencyIso.equals(withdrawCurrencyIso)) {
            response.setRate(BigDecimal.ONE);
        } else if ("USD".equals(acceptedCurrencyIso) && "EUR".equals(withdrawCurrencyIso)) {
            response.setRate(new BigDecimal("0.85"));
        } else if ("EUR".equals(acceptedCurrencyIso) && "USD".equals(withdrawCurrencyIso)) {
            response.setRate(new BigDecimal("1.18"));
        } else {
            response.setRate(BigDecimal.ONE);
        }
        return response;
    }

    @Override
    protected String getPaymentPointCode(PaymentTransferRequestData sendingTransferRequest) {
        return null;
    }

    /**
     * Stub implementation: creates test transfer data.
     * This replaces the previous payment system API integration.
     */
    @Override
    protected PaymentTransferData create(User user, PaymentTransferRequestData sendingTransferRequest) {
        // Stub: creates test transfer instead of calling real API
        // Currently returns test data - you can implement your own API for transfer creation
        web.service.pat.transferdata.transfer.Response response = createTestTransferResponse(sendingTransferRequest);
        TransferOperation transferOperation = createTransferOperation(user, sendingTransferRequest,
                                                                      response.getPayAmount().add(response.getCommission())
                                                                              .add(response.getAgentCommission()), response.getControlNumber(),
                                                                      response.getAgentCommission(), response.getCommission());
        PaymentTransferData transferData = buildTransferData(response, web.service.pat.Sender::new, web.service.pat.Receiver::new);
        transferData.setTransferOperation(transferOperation);
        return transferData;
    }
    
    /**
     * Creates test transfer response for demonstration.
     */
    private web.service.pat.transferdata.transfer.Response createTestTransferResponse(PaymentTransferRequestData request) {
        web.service.pat.transferdata.transfer.Response response = new web.service.pat.transferdata.transfer.Response();
        // Generate test control number
        response.setControlNumber("TEST" + System.currentTimeMillis());
        response.setCountry(request.getDestinationCountry().getId());
        response.setRegion(request.getDestinationRegion().getName());
        response.setAmount(request.getAmount());
        response.setRate(request.getRate());
        response.setPayAmount(request.getPayAmount());
        response.setCommission(request.getCommission());
        response.setAgentCommission(request.getAgentCommission());
        response.setAcceptedCurrency(request.getAcceptedCurrency().getIso());
        response.setWithdrawCurrency(request.getTransferCurrency().getIso());
        
        // Create test sender
        web.service.pat.transferdata.Sender sender = new web.service.pat.transferdata.Sender();
        sender.setLastname(request.getPerson().getLastname());
        sender.setFirstname(request.getPerson().getFirstname());
        sender.setPatronymic(request.getPerson().getPatronymic());
        sender.setBirthDate(request.getPerson().getBirthDate());
        sender.setCitizenship(request.getPerson().getCitizenship());
        response.setSender(sender);
        
        // Create test receiver
        web.service.pat.transferdata.Receiver receiver = new web.service.pat.transferdata.Receiver();
        receiver.setLastname(request.getRecipient().getLastname());
        receiver.setFirstname(request.getRecipient().getFirstname());
        receiver.setPatronymic(request.getRecipient().getPatronymic());
        receiver.setBirthDate(request.getRecipient().getBirthDate());
        receiver.setCitizenshipCode(request.getRecipient().getCitizenship());
        response.setReceiver(receiver);
        
        return response;
    }

    @Override
    protected String getBackPaymentSystemName() {
        return "MONEY_TRANSFER";
    }

    private <T extends AbstractTransferResponse> PaymentTransferData buildTransferData(T response, Supplier<web.service.pat.Sender> senderSupplier,
                                                                                      Supplier<web.service.pat.Receiver> receiverSupplier) {
        PaymentTransferData transferData = new PaymentTransferData();
        transferData.setDestinationCountry(countryRepository.findOne(response.getCountry()));
        transferData.setDestinationRegion(regionRepository.findOne((root, query, cb) -> cb.and(cb.equal(root.get(Region_.name), response.getRegion()),
                                                                                               cb.equal(root.get(Region_.country),
                                                                                                        transferData.getDestinationCountry()))));
        if (response.getAcceptedCurrency() != null) {
            transferData.setAcceptedCurrency(currencyRepository.findByIso(response.getAcceptedCurrency()));
        }
        transferData.setTransferCurrency(currencyRepository.findByIso(response.getWithdrawCurrency()));
        transferData.setAmount(response.getAmount());
        transferData.setRate(response.getRate());
        transferData.setCommission(response.getCommission());
        transferData.setAgentCommission(response.getAgentCommission());
        web.service.pat.Sender sender = senderSupplier.get();
        sender.setDocument(new web.service.pat.Document());
        sender.setAddress(new web.service.pat.Address());
        BeanUtils.copyProperties(response.getSender(), sender);
        BeanUtils.copyProperties(response.getSender().getDocument(), sender.getDocument());
        BeanUtils.copyProperties(response.getSender().getAddress(), sender.getAddress());
        transferData.setSender(sender);
        web.service.pat.Receiver receiver = receiverSupplier.get();
        BeanUtils.copyProperties(response.getReceiver(), receiver);
        if (response.getReceiver().getDocument() != null) {
            receiver.setDocument(new web.service.pat.Document());
            BeanUtils.copyProperties(response.getReceiver().getDocument(), receiver.getDocument());
        }
        receiver.setAddress(new web.service.pat.Address());
        BeanUtils.copyProperties(response.getReceiver().getAddress(), receiver.getAddress());
        transferData.setReceiver(receiver);
        return transferData;
    }

    /**
     * Stub implementation: blocks test transfer.
     */
    @Override
    protected SimpleReceivingTransferResponse block(User user, PayoutTransferRequestData payoutTransferData) {
        // Stub: no actual API call, just returns success
        return new SimpleReceivingTransferResponse();
    }

    @Override
    protected DirectionType getPayoutDirectionType(PayoutTransferRequestData payoutTransferData,
                                                   SimpleReceivingTransferResponse receivingTransferResponse) {
        // Always return INTERNATIONAL for universal payment system
        return DirectionType.INTERNATIONAL;
    }

    /**
     * Stub implementation: finds test transfer.
     */
    @Override
    public PaymentTransferData findTransfer(String departmentCode, String controlNumber) {
        // Stub: returns test transfer if control number starts with "TEST"
        // Currently returns test data - you can implement your own API for transfer search
        if (controlNumber != null && controlNumber.startsWith("TEST")) {
            web.service.pat.transferdata.search.Response response = new web.service.pat.transferdata.search.Response();
            response.setStatus(27101); // Success status
            response.setControlNumber(controlNumber);
            response.setCountry("US");
            response.setRegion("New York");
            response.setAmount(new BigDecimal("100.00"));
            response.setRate(BigDecimal.ONE);
            response.setPayAmount(new BigDecimal("100.00"));
            response.setCommission(new BigDecimal("2.00"));
            response.setAgentCommission(new BigDecimal("1.00"));
            response.setAcceptedCurrency("USD");
            response.setWithdrawCurrency("USD");
            
            PaymentTransferData paymentTransferData = buildTransferData(response, ExtendedSender::new, ExtendedReceiver::new);
            paymentTransferData.setStatus(response.getStatus());
            return paymentTransferData;
        } else {
            return null;
        }
    }

    /**
     * Stub implementation: cancels test receiving transfer.
     */
    @Override
    protected void cancelReceiving(String code, String departmentCode) {
        // Stub: no actual API call
    }

    /**
     * Stub implementation: confirms test receiving transfer.
     */
    @Override
    protected void confirmReceiving(ReceivingConfirmData receivingConfirmData) {
        // Stub: no actual API call, just logs the confirmation
    }

    /**
     * Stub implementation: cancels test sending transfer.
     */
    @Override
    protected void cancelSending(SendingCancelData sendingCancelData) {
        // Stub: no actual API call
    }

    /**
     * Stub implementation: confirms test sending transfer.
     */
    @Override
    protected void confirmSending(SendingConfirmData sendingConfirmData) {
        // Stub: no actual API call, just logs the confirmation
    }
}
