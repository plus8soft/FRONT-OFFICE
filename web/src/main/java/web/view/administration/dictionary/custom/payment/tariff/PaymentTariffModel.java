/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.tariff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import web.entity.dict.PaymentTariff;
import web.entity.dict.PaymentTariff_;
import web.repository.dict.PaymentTariffRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class PaymentTariffModel extends AbstractVirtualScrollLazyModel<PaymentTariff, Long> {

    @Autowired
    private PaymentTariffRepository paymentTariffRepository;

    private PaymentTariffFilter filter;

    @Override
    protected Function<PaymentTariff, Long> keyFunction() {
        return PaymentTariff::getId;
    }

    @Override
    protected long count() {
        return paymentTariffRepository.count(getSpecification());
    }

    @Override
    public List<PaymentTariff> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return paymentTariffRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<PaymentTariff> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getName())) {
                predicates.add(cb.like(cb.upper(root.get(PaymentTariff_.name)), String.format("%s%%", filter.getName().toUpperCase())));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getStatus())) {
                    predicates.add(cb.equal(root.get(PaymentTariff_.enabled), filter.getStatus()));
                }
                if (Objects.nonNull(filter.getPaymentSystem())) {
                    predicates.add(cb.equal(root.get(PaymentTariff_.paymentSystem), filter.getPaymentSystem()));
                }
                if (Objects.nonNull(filter.getDestinationRequired())) {
                    predicates.add(cb.equal(root.get(PaymentTariff_.destinationRequired), filter.getDestinationRequired()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
