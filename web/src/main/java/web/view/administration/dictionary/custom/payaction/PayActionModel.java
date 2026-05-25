/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payaction;

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
import web.entity.dict.PayAction;
import web.entity.dict.PayAction_;
import web.repository.dict.PayActionRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class PayActionModel extends AbstractVirtualScrollLazyModel<PayAction, Long> {

    @Autowired
    private PayActionRepository payActionRepository;

    private PayActionFilter filter;

    private PayAction selected;

    @Override
    protected Function<PayAction, Long> keyFunction() {
        return PayAction::getId;
    }

    @Override
    protected long count() {
        return payActionRepository.count(getSpecification());
    }

    @Override
    public List<PayAction> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return payActionRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<PayAction> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getName())) {
                predicates.add(cb.like(root.get(PayAction_.name), String.format("%s%%", filter.getName())));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getType())) {
                    predicates.add(cb.equal(root.get(PayAction_.type), filter.getType()));
                }
                if (Objects.nonNull(filter.getCashSymbol())) {
                    predicates.add(cb.equal(root.get(PayAction_.cashSymbol), filter.getCashSymbol()));
                }
                if (Objects.nonNull(filter.getDisabled())) {
                    predicates.add(cb.equal(root.get(PayAction_.disabled), filter.getDisabled()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
