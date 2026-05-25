/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

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
import web.entity.dict.Account_;
import web.entity.dict.Counteragent;
import web.entity.dict.CounteragentPayAction_;
import web.entity.dict.Counteragent_;
import web.repository.dict.CounteragentRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class CounteragentModel extends AbstractVirtualScrollLazyModel<Counteragent, Long> {

    @Autowired
    private CounteragentRepository counteragentRepository;

    private Counteragent selected;

    private CounteragentFilter filter;

    @Override
    protected Function<Counteragent, Long> keyFunction() {
        return Counteragent::getId;
    }

    @Override
    protected long count() {
        return counteragentRepository.count(getSpecification());
    }

    @Override
    public List<Counteragent> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return counteragentRepository.findAll((root, query, cb) -> {
            root.fetch(Counteragent_.user, JoinType.LEFT);
            return getSpecification().toPredicate(root, query, cb);
        }, first, pageSize, new Sort(Counteragent_.ein.getName(), Counteragent_.version.getName()));
    }

    private Specification<Counteragent> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getEin())) {
                predicates.add(cb.like(root.get(Counteragent_.ein), String.format("%s%%", filter.getEin())));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getName())) {
                    predicates.add(cb.like(cb.upper(root.get(Counteragent_.name)), String.format("%s%%", filter.getName().toUpperCase())));
                }
                if (Objects.nonNull(filter.getHasContract())) {
                    predicates.add(cb.equal(root.get(Counteragent_.contract), filter.getHasContract()));
                }
                if (Objects.nonNull(filter.getAddress())) {
                    predicates.add(cb.like(root.get(Counteragent_.address), String.format("%s%%", filter.getAddress())));
                }
                if (Objects.nonNull(filter.getAccount())) {
                    predicates.add(cb.like(root.join(Counteragent_.payActions).join(CounteragentPayAction_.account).get(Account_.id),
                                           String.format("%s%%", filter.getAccount())));
                    query.distinct(true);
                }
                if (Objects.nonNull(filter.getDisabled())) {
                    predicates.add(cb.equal(root.get(Counteragent_.disabled), filter.getDisabled()));
                }
                if (Objects.nonNull(filter.getBank())) {
                    predicates.add(cb.equal(root.get(Counteragent_.routingNumber), filter.getBank().getRoutingNumber()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
