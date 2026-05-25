/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.SortMeta;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import web.entity.log.AbstractEvent;
import web.entity.log.AbstractEvent_;
import web.entity.log.ConnectionEvent_;
import web.repository.log.AbstractEventRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
public abstract class AbstractEventModel<T extends AbstractEvent> extends AbstractVirtualScrollLazyModel<T, Long> {

    private T selected;

    private EventFilter filter;

    private List<T> events;

    public abstract AbstractEventRepository<T> getEventRepository();

    @Override
    protected Function<T, Long> keyFunction() {
        return AbstractEvent::getId;
    }

    @Override
    protected long count() {
        return getEventRepository().count(getSpecification());
    }

    @Override
    public List<T> loadData(int offset, int pageSize, List<SortMeta> multiSortData, Map<String, Object> filters) {
        return getEventRepository().findAll(getSpecification(), offset, pageSize, new Sort(Sort.Direction.DESC, AbstractEvent_.date.getName()));
    }

    private Specification<T> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getEventId())) {
                predicates.add(cb.equal(root.get(AbstractEvent_.connectionEvent).get(ConnectionEvent_.id), filter.getEventId()));
            }
            if (!filter.getTypes().isEmpty()) {
                predicates.add(root.get(AbstractEvent_.status).in(filter.getTypes()));
            }
            if (!filter.getCodes().isEmpty()) {
                predicates.add(root.get(AbstractEvent_.code).in(filter.getCodes()));
            }
            if (Objects.nonNull(filter.getEventDateWith())) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(AbstractEvent_.date), filter.getEventDateWith()));
            }
            if (Objects.nonNull(filter.getEventDate())) {
                predicates.add(cb.lessThanOrEqualTo(root.get(AbstractEvent_.date), filter.getEventDate()));
            }
            if (!filter.getUsers().isEmpty()) {
                predicates.add(root.get(AbstractEvent_.user).in(filter.getUsers()));
            }
            if (!filter.getDepartments().isEmpty()) {
                predicates.add(root.get(AbstractEvent_.department).in(filter.getDepartments()));
            }
            if (!filter.getProjects().isEmpty()) {
                predicates.add(root.get(AbstractEvent_.project).in(filter.getProjects()));
            }
            if (!filter.getTasks().isEmpty()) {
                predicates.add(root.get(AbstractEvent_.task).in(filter.getTasks()));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    void loadAllEvents() {
        events = getEventRepository().findAll(getSpecification(), new Sort(Sort.Direction.DESC, AbstractEvent_.date.getName()));
    }
}
