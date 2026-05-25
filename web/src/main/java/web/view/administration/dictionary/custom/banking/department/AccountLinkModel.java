/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.department;

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
import web.entity.dict.AccountLink;
import web.entity.dict.AccountLink_;
import web.entity.dict.Account_;
import web.repository.dict.AccountLinkRepository;
import web.view.administration.dictionary.custom.banking.account.AccountFilter;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class AccountLinkModel extends AbstractVirtualScrollLazyModel<AccountLink, Long> {

    @Autowired
    private AccountLinkRepository accountLinkRepository;

    private AccountFilter filter;

    @Override
    protected Function<AccountLink, Long> keyFunction() {
        return AccountLink::getId;
    }

    @Override
    protected long count() {
        return accountLinkRepository.count(getSpecification());
    }

    @Override
    public List<AccountLink> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return accountLinkRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<AccountLink> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getDepartment())) {
                predicates.add(cb.equal(root.get(AccountLink_.department), filter.getDepartment()));
            }
            if (Objects.nonNull(filter.getNumberAccount())) {
                predicates.add(cb.like(root.get(AccountLink_.account).get(Account_.id), String.format("%s%%", filter.getNumberAccount())));
            }
            if (Objects.nonNull(filter.getStatus())) {
                predicates.add(cb.equal(root.get(AccountLink_.account).get(Account_.enabled), filter.getStatus()));
            }
            if (!filter.getCurrencies().isEmpty()) {
                predicates.add(root.get(AccountLink_.account).get(Account_.currency).in(filter.getCurrencies()));
            }
            if (Objects.nonNull(filter.getName())) {
                predicates.add(cb.like(cb.upper(root.get(AccountLink_.account).get(Account_.name)),
                                       String.format("%s%%", filter.getName().toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
