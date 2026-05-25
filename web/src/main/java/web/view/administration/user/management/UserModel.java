/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.management;

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
import org.springframework.data.jpa.domain.Specification;
import web.entity.core.User;
import web.entity.core.User_;
import web.repository.core.UserRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class UserModel extends AbstractVirtualScrollLazyModel<User, Long> {

    @Autowired
    private UserRepository userRepository;

    private UserFilter filter;

    private User selected;

    @Override
    protected Function<User, Long> keyFunction() {
        return User::getId;
    }

    @Override
    protected long count() {
        return userRepository.count(getSpecification());
    }

    @Override
    public List<User> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return userRepository.findAll((root, query, cb) -> {
            root.fetch(User_.department, JoinType.LEFT);
            root.fetch(User_.securityProfile, JoinType.LEFT);
            return getSpecification().toPredicate(root, query, cb);
        }, first, pageSize);
    }

    private Specification<User> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getTextFilter())) {
                String text = String.format("%s%%", filter.getTextFilter().toUpperCase());
                predicates.add(cb.or(cb.or(cb.like(cb.upper(root.get(User_.firstname)), text), cb.like(cb.upper(root.get(User_.lastname)), text)),
                                     cb.or(cb.like(cb.upper(root.get(User_.patronymic)), text), cb.like(cb.upper(root.get(User_.login)), text))));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getPhone())) {
                    predicates.add(cb.or(cb.like(root.get(User_.mobilePhone), String.format("%s%%", filter.getPhone())),
                                         cb.like(root.get(User_.workPhone), String.format("%s%%", filter.getPhone()))));
                }
                if (Objects.nonNull(filter.getEmail())) {
                    predicates.add(cb.like(cb.upper(root.get(User_.email)), String.format("%s%%", filter.getEmail().toUpperCase())));
                }
                if (Objects.nonNull(filter.getStatus())) {
                    predicates.add(cb.equal(root.get(User_.status), filter.getStatus()));
                }
                if (Objects.nonNull(filter.getAccountExpirationDateFrom())) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(User_.accountExpirationDate), filter.getAccountExpirationDateFrom()));
                }
                if (Objects.nonNull(filter.getAccountExpirationDateTo())) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(User_.accountExpirationDate), filter.getAccountExpirationDateTo()));
                }
                if (Objects.nonNull(filter.getSecurityProfile())) {
                    predicates.add(cb.equal(root.get(User_.securityProfile), filter.getSecurityProfile()));
                }
                if (Objects.nonNull(filter.getPosition())) {
                    predicates.add(cb.equal(root.get(User_.position), filter.getPosition()));
                }
                if (!filter.getDepartments().isEmpty()) {
                    predicates.add(root.get(User_.department).in(filter.getDepartments()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
