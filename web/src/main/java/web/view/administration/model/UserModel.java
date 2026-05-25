/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.SetJoin;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import web.entity.core.Group;
import web.entity.core.Group_;
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
        return userRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<User> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getUserGroup())) {
                SetJoin<User, Group> userGroupJoin = root.join(User_.groups, JoinType.LEFT);
                predicates.add(cb.and(cb.isTrue(userGroupJoin.get(Group_.areUser)), cb.equal(userGroupJoin, filter.getUserGroup())));
            }
            if (Objects.nonNull(filter.getTextFilter())) {
                String text = String.format("%s%%", filter.getTextFilter().toUpperCase());
                predicates.add(cb.or(cb.or(cb.like(cb.upper(root.get(User_.firstname)), text), cb.like(cb.upper(root.get(User_.lastname)), text)),
                                     cb.or(cb.like(cb.upper(root.get(User_.patronymic)), text), cb.like(cb.upper(root.get(User_.login)), text))));
            }
            if (Objects.nonNull(filter.getDepartment())) {
                predicates.add(cb.equal(root.get(User_.department), filter.getDepartment()));
            }
            if (Objects.nonNull(filter.getDepartmentGroup())) {
                predicates.add(cb.isMember(filter.getDepartmentGroup(), root.get(User_.groups)));
            }
            if (Objects.nonNull(filter.getRole())) {
                predicates.add(cb.isMember(filter.getRole(), root.get(User_.roles)));
            }
            if (Objects.nonNull(filter.getRight())) {
                predicates.add(cb.isMember(filter.getRight(), root.get(User_.rights)));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getLastLoginEventDate())) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(User_.lastLoginEventDate), filter.getLastLoginEventDate()));
                }
                if (Objects.nonNull(filter.getStatus())) {
                    predicates.add(cb.equal(root.get(User_.status), filter.getStatus()));
                }
                if (Objects.nonNull(filter.getPosition())) {
                    predicates.add(cb.equal(root.get(User_.position), filter.getPosition()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
