/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.account;

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
import web.entity.dict.Account;
import web.entity.dict.Account_;
import web.repository.dict.AccountRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class AccountModel extends AbstractVirtualScrollLazyModel<Account, String> {

    @Autowired
    private AccountRepository accountRepository;

    private AccountFilter filter;

    private Account selected;

    @Override
    protected Function<Account, String> keyFunction() {
        return Account::getId;
    }

    @Override
    protected long count() {
        return accountRepository.count(getSpecification());
    }

    @Override
    public List<Account> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return accountRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<Account> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getName())) {
                predicates.add(cb.like(cb.upper(root.get(Account_.name)), String.format("%s%%", filter.getName().toUpperCase())));
            }
            if (Objects.nonNull(filter.getStatus())) {
                predicates.add(cb.equal(root.get(Account_.enabled), filter.getStatus()));
            }
            if (Objects.nonNull(filter.getNumberAccount())) {
                predicates.add(cb.like(root.get(Account_.id), String.format("%s%%", filter.getNumberAccount())));
            }
            if (!filter.getCurrencies().isEmpty()) {
                predicates.add(root.get(Account_.currency).in(filter.getCurrencies()));
            }
            if (Objects.nonNull(filter.getDepartment())) {
                predicates.add(cb.equal(root.get(Account_.department), filter.getDepartment()));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
