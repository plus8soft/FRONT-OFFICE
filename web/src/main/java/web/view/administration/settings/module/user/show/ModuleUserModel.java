/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.settings.module.user.show;

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
import web.entity.core.Group_;
import web.entity.core.Role_;
import web.entity.core.User;
import web.entity.core.User_;
import web.repository.core.UserRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class ModuleUserModel extends AbstractVirtualScrollLazyModel<User, Long> {

    @Autowired
    private UserRepository userRepository;

    private ModuleUserFilter filter;

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
        return userRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<User> getSpecification() {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.or(cb.isMember(filter.getTask(), root.get(User_.tasks)),
                                 cb.equal(root.join(User_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT), filter.getTask()),
                                 cb.equal(root.join(User_.groups, JoinType.LEFT).join(Group_.roles, JoinType.LEFT).join(Role_.tasks, JoinType.LEFT),
                                          filter.getTask())));
            if (Objects.nonNull(filter.getTextFilter())) {
                String text = String.format("%s%%", filter.getTextFilter().toUpperCase());
                predicates.add(cb.or(cb.or(cb.like(cb.upper(root.get(User_.firstname)), text), cb.like(cb.upper(root.get(User_.lastname)), text)),
                                     cb.or(cb.like(cb.upper(root.get(User_.patronymic)), text), cb.like(cb.upper(root.get(User_.login)), text))));
            }
            if (filter.isExtendedSearch()) {
                if (!filter.getUserStatuses().isEmpty()) {
                    predicates.add(root.get(User_.status).in(filter.getUserStatuses()));
                }
                if (Objects.nonNull(filter.getExpirationDate())) {
                    predicates.add(cb.lessThan(root.get(User_.accountExpirationDate), filter.getExpirationDate()));
                }
                if (!filter.getSecurityProfiles().isEmpty()) {
                    predicates.add(root.get(User_.securityProfile).in(filter.getSecurityProfiles()));
                }
                if (!filter.getDepartmentByNames().isEmpty()) {
                    predicates.add(root.get(User_.department).in(filter.getDepartmentByNames()));
                }
                if (!filter.getDepartmentByCodes().isEmpty()) {
                    predicates.add(root.get(User_.department).in(filter.getDepartmentByCodes()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
