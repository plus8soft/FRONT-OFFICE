/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.rate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import web.entity.ce.RateType;
import web.entity.dict.Currency_;
import web.entity.dict.ExtRate;
import web.entity.dict.ExtRate_;
import web.repository.dict.ExtRateRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class ExtRateModel extends AbstractVirtualScrollLazyModel<ExtRate, Long> {

    @Autowired
    private ExtRateRepository extRateRepository;

    private ExtRateFilter filter;

    @Override
    protected Function<ExtRate, Long> keyFunction() {
        return ExtRate::getId;
    }

    @Override
    protected long count() {
        return extRateRepository.count(getSpecification());
    }

    @Override
    public List<ExtRate> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return extRateRepository.findAll((root, query, cb) -> {
            root.fetch(ExtRate_.currency, JoinType.LEFT);
            return getSpecification().toPredicate(root, query, cb);
        }, first, pageSize, new Sort(Sort.Direction.DESC, ExtRate_.date.getName())
                                                 .and(new Sort(ExtRate_.currency.getName() + "." + Currency_.position.getName())));
    }

    private Specification<ExtRate> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(ExtRate_.type), RateType.EXTERNAL));
            if (Objects.nonNull(filter.getCurrency())) {
                predicates.add(cb.equal(root.get(ExtRate_.currency), filter.getCurrency()));
            }
            if (Objects.nonNull(filter.getDate())) {
                predicates.add(cb.equal(root.get(ExtRate_.date), filter.getDate()));
            }
            if (Objects.nonNull(filter.getRatio())) {
                predicates.add(cb.equal(root.get(ExtRate_.ratio), filter.getRatio()));
            }
            if (Objects.nonNull(filter.getBuyRate())) {
                predicates.add(cb.equal(root.get(ExtRate_.buyRate), filter.getBuyRate()));
            }
            if (Objects.nonNull(filter.getSellRate())) {
                predicates.add(cb.equal(root.get(ExtRate_.sellRate), filter.getSellRate()));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
