/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict.rate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.entity.ce.RateType;
import web.entity.dict.Currency;
import web.entity.dict.ExtRate;
import web.entity.dict.ExtRate_;
import web.entity.dict.UpdateResult;
import web.repository.dict.CurrencyRepository;
import web.repository.dict.ExtRateRepository;
import web.service.dict.DictionaryUpdateResult;
import web.service.stubs.ratestub.ExternalRateService;
import web.service.stubs.ratestub.MarketRateService;

@Service
@Transactional
public class ExtRateService {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExtRateRepository extRateRepository;

    @Autowired
    private ExternalRateService externalRateService;

    @Autowired
    private MarketRateService marketRateService;

    private RateMapper<ExtRate> externalRateMapper;

    private RateMapper<ExtRate> marketRateMapper;

    @PostConstruct
    private void init() {
        externalRateMapper = (id, ratio, sell, buy, date) -> {
            ExtRate extRate = null;
            Currency currency = currencyRepository.findOne(id);
            if (currency != null) {
                extRate = new ExtRate();
                extRate.setCurrency(currency);
                extRate.setDate(date.truncatedTo(ChronoUnit.SECONDS));
                extRate.setRatio(ratio);
                extRate.setType(RateType.EXTERNAL);
                extRate.setBuyRate(buy);
                extRate.setSellRate(sell);
            }
            return extRate;
        };
        marketRateMapper = (id, ratio, sell, buy, date) -> {
            ExtRate extRate = null;
            Currency currency = currencyRepository.findOne(id);
            if (currency != null) {
                extRate = new ExtRate();
                extRate.setCurrency(currency);
                extRate.setDate(date.truncatedTo(ChronoUnit.SECONDS));
                extRate.setRatio(ratio);
                extRate.setType(RateType.MARKET);
                extRate.setBuyRate(buy);
                extRate.setSellRate(sell);
            }
            return extRate;
        };
    }

    /**
     * Loads external currency rates from stub service.
     * 
     * Note: This method uses a stub implementation that returns empty data.
     * To enable real rate fetching, implement ExternalRateService with actual API integration.
     * 
     * @return list of external rates (empty list from stub)
     */
    public List<ExtRate> loadExternalRates() {
        // Stub implementation: returns empty list
        // The externalRateService is a stub that doesn't fetch real data
        // To implement real integration, replace ExternalRateService stub with actual API calls
        List<ExtRate> extRates = new ArrayList<>();
        LocalDateTime localDateTime = externalRateService.latestDate();
        LocalDateTime earliestDate = LocalDateTime.now().minusDays(1);
        if (earliestDate.isAfter(localDateTime)) {
            earliestDate = localDateTime;
        }
        do {
            List<ExtRate> rates = externalRateService.rates(localDateTime, externalRateMapper).stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (earliestDate.equals(localDateTime) && rates != null && rates.isEmpty()) {
                while (rates.isEmpty()) {
                    rates = externalRateService.rates(localDateTime = localDateTime.minusDays(1), externalRateMapper).stream().filter(Objects::nonNull)
                                          .collect(Collectors.toList());
                }
            }
            extRates.addAll(rates);
        } while (!(localDateTime = localDateTime.minusDays(1)).isBefore(earliestDate));
        return extRateRepository.save(extRates.stream().filter(Objects::nonNull).filter(extRate -> !extRateRepository.exists((root, query, cb) -> cb
                .and(cb.equal(root.get(ExtRate_.type), RateType.EXTERNAL), cb.equal(root.get(ExtRate_.currency), extRate.getCurrency()),
                     cb.equal(root.get(ExtRate_.date), extRate.getDate())))).collect(Collectors.toList()));
    }

    /**
     * Loads market currency rates from stub service.
     * 
     * Note: This method uses a stub implementation that returns hardcoded test data.
     * Currently returns hardcoded values - you can implement your own API for market rates.
     * 
     * @return list of market rates (hardcoded test data from stub)
     */
    public void loadMarketRates() {
        extRateRepository.save(marketRateService.loadRates(marketRateMapper).stream().filter(Objects::nonNull).filter(extRate -> !extRateRepository
                .exists((root, query, cb) -> cb
                        .and(cb.equal(root.get(ExtRate_.type), RateType.MARKET), cb.equal(root.get(ExtRate_.currency), extRate.getCurrency()),
                             cb.equal(root.get(ExtRate_.date), extRate.getDate())))).collect(Collectors.toList()));
    }

    public DictionaryUpdateResult update() {
        List<ExtRate> extRates = loadExternalRates();
        DictionaryUpdateResult dictionaryUpdateResult = new DictionaryUpdateResult();
        if (extRates != null && !extRates.isEmpty()) {
            dictionaryUpdateResult.setUpdateDate(extRates.stream().findAny().map(ExtRate::getDate).orElse(LocalDateTime.now()));
        } else {
            dictionaryUpdateResult.setUpdateDate(LocalDateTime.now());
        }
        dictionaryUpdateResult.setUpdateResult(UpdateResult.SUCCESSFULLY);
        return dictionaryUpdateResult;
    }
}
