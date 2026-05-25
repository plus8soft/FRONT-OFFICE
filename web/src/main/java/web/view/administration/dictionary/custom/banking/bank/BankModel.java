/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.bank;

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
import web.entity.dict.Bank;
import web.entity.dict.Bank_;
import web.repository.dict.BankRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class BankModel extends AbstractVirtualScrollLazyModel<Bank, Long> {

    @Autowired
    private BankRepository bankRepository;

    private BankFilter filter;

    private Bank selected;

    @Override
    protected Function<Bank, Long> keyFunction() {
        return Bank::getId;
    }

    @Override
    protected long count() {
        return bankRepository.count(getSpecification());
    }

    @Override
    public List<Bank> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return bankRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<Bank> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getRoutingNumber())) {
                predicates.add(cb.like(root.get(Bank_.routingNumber), String.format("%s%%", filter.getRoutingNumber())));
            }
            if (Objects.nonNull(filter.getCorrespondentAccount())) {
                predicates.add(cb.like(root.get(Bank_.correspondentAccount), String.format("%s%%", filter.getCorrespondentAccount())));
            }
            if (Objects.nonNull(filter.getName())) {
                predicates.add(cb.like(cb.upper(root.get(Bank_.name)), String.format("%s%%", filter.getName().toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
