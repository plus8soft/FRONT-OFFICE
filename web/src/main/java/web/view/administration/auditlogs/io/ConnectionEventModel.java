/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import web.entity.AuthorizationResult;
import web.entity.core.User;
import web.entity.core.User_;
import web.entity.log.ConnectionEvent;
import web.entity.log.ConnectionEvent_;
import web.repository.log.ConnectionEventRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class ConnectionEventModel extends AbstractVirtualScrollLazyModel<ConnectionEvent, Long> {

    @Autowired
    private ConnectionEventRepository connectionEventRepository;

    private ConnectionEvent selected;

    private ConnectionEventFilter filter;

    private List<ConnectionEvent> connectionEventList;

    @Override
    protected Function<ConnectionEvent, Long> keyFunction() {
        return ConnectionEvent::getId;
    }

    @Override
    protected long count() {
        return connectionEventRepository.count(getSpecification());
    }

    @Override
    public List<ConnectionEvent> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return connectionEventRepository.findAll((root, query, cb) -> {
            root.fetch(ConnectionEvent_.user, JoinType.LEFT);
            return getSpecification().toPredicate(root, query, cb);
        }, first, pageSize, new Sort(Sort.Direction.DESC, ConnectionEvent_.date.getName()));
    }

    private Specification<ConnectionEvent> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getFirstname()) || Objects.nonNull(filter.getLastname()) || Objects.nonNull(filter.getPatronymic())) {
                Join<ConnectionEvent, User> join = root.join(ConnectionEvent_.user);
                if (Objects.nonNull(filter.getFirstname())) {
                    predicates.add(cb.like(cb.upper(join.get(User_.firstname)), String.format("%s%%", filter.getFirstname().toUpperCase())));
                }
                if (Objects.nonNull(filter.getLastname())) {
                    predicates.add(cb.like(cb.upper(join.get(User_.lastname)), String.format("%s%%", filter.getLastname().toUpperCase())));
                }
                if (Objects.nonNull(filter.getPatronymic())) {
                    predicates.add(cb.like(cb.upper(join.get(User_.patronymic)), String.format("%s%%", filter.getPatronymic().toUpperCase())));
                }
            }
            if (Objects.nonNull(filter.getUserLogin())) {
                predicates.add(cb.like(cb.upper(root.get(ConnectionEvent_.login)), String.format("%s%%", filter.getUserLogin().toUpperCase())));
            }
            if (Objects.nonNull(filter.getUserIp())) {
                predicates.add(cb.like(cb.upper(root.get(ConnectionEvent_.ip)), String.format("%s%%", filter.getUserIp().toUpperCase())));
            }
            if (Objects.nonNull(filter.getResult())) {
                if (filter.getResult()) {
                    predicates.add(cb.equal(root.get(ConnectionEvent_.authorizationResult), AuthorizationResult.SUCCESS));
                } else {
                    predicates.add(cb.notEqual(root.get(ConnectionEvent_.authorizationResult), AuthorizationResult.SUCCESS));
                }
            }
            if (!filter.getAuthorizationResults().isEmpty()) {
                predicates.add(root.get(ConnectionEvent_.authorizationResult).in(filter.getAuthorizationResults()));
            }
            if (Objects.nonNull(filter.getEventDateWith())) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(ConnectionEvent_.date), filter.getEventDateWith()));
            }
            if (Objects.nonNull(filter.getEventDate())) {
                predicates.add(cb.lessThanOrEqualTo(root.get(ConnectionEvent_.date), filter.getEventDate()));
            }
            if (Objects.nonNull(filter.getOutDateWith())) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(ConnectionEvent_.logoffDate), filter.getOutDateWith()));
            }
            if (Objects.nonNull(filter.getOutDate())) {
                predicates.add(cb.lessThanOrEqualTo(root.get(ConnectionEvent_.logoffDate), filter.getOutDate()));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public void loadAllConnectionEvents() {
        connectionEventList = connectionEventRepository.findAll(getSpecification(), new Sort(Sort.Direction.DESC, ConnectionEvent_.date.getName()));
    }
}
