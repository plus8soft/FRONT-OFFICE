/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.log.operation;

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
import web.entity.core.Department;
import web.entity.core.User;
import web.entity.crm.BaseDocument_;
import web.entity.log.Operation;
import web.entity.log.Operation_;
import web.entity.log.PersonHistory;
import web.entity.log.PersonHistory_;
import web.repository.log.OperationRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class OperationModel extends AbstractVirtualScrollLazyModel<Operation, Long> {

    @Autowired
    private OperationRepository operationRepository;

    private OperationFilter filter;

    private Operation selected;

    private List<Department> defaultDepartments;

    private List<User> defaultUsers;

    @Override
    protected Function<Operation, Long> keyFunction() {
        return Operation::getId;
    }

    @Override
    protected long count() {
        return operationRepository.count(getSpecification());
    }

    @Override
    public List<Operation> loadData(int offset, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return operationRepository.findAll((root, query, cb) -> {
            root.fetch(Operation_.personHistory, JoinType.LEFT);
            root.fetch(Operation_.user, JoinType.LEFT);
            root.fetch(Operation_.task, JoinType.LEFT);
            return getSpecification().toPredicate(root, query, cb);
        }, offset, pageSize, new Sort(Sort.Direction.DESC, Operation_.instant.getName()));
    }

    private Specification<Operation> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getStartDate())) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(Operation_.instant), filter.getStartDate()));
            }
            if (Objects.nonNull(filter.getEndDate())) {
                predicates.add(cb.lessThanOrEqualTo(root.get(Operation_.instant), filter.getEndDate()));
            }
            if (filter.getDepartmentsByName().isEmpty()) {
                if (defaultDepartments != null) {
                    predicates.add(root.get(Operation_.department).in(defaultDepartments));
                }
            } else {
                predicates.add(root.get(Operation_.department).in(filter.getDepartmentsByName()));
            }
            if (filter.getUsers().isEmpty()) {
                if (defaultUsers != null) {
                    predicates.add(root.join(Operation_.user).in(defaultUsers));
                }
            } else {
                predicates.add(root.join(Operation_.user).in(filter.getUsers()));
            }
            if (filter.isExtendedSearch()) {
                if (!filter.getStatuses().isEmpty()) {
                    predicates.add(root.get(Operation_.status).in(filter.getStatuses()));
                }
                if (!filter.getCodes().isEmpty()) {
                    predicates.add(root.get(Operation_.code).in(filter.getCodes()));
                }
                if (Objects.nonNull(filter.getTask())) {
                    predicates.add(cb.equal(root.get(Operation_.task), filter.getTask()));
                }
                if (Objects.nonNull(filter.getFirstname()) || Objects.nonNull(filter.getLastname()) || Objects.nonNull(filter.getPatronymic()) ||
                    Objects.nonNull(filter.getBirthdate()) || Objects.nonNull(filter.getDocumentSeries()) ||
                    Objects.nonNull(filter.getDocumentNumber()) || Objects.nonNull(filter.getDocumentSeries()) ||
                    Objects.nonNull(filter.getDocumentNumber())) {
                    Join<Operation, PersonHistory> join = root.join(Operation_.personHistory);
                    if (Objects.nonNull(filter.getFirstname())) {
                        predicates.add(cb.like(cb.upper(join.get(PersonHistory_.firstname)),
                                               String.format("%s%%", filter.getFirstname().toUpperCase())));
                    }
                    if (Objects.nonNull(filter.getLastname())) {
                        predicates
                                .add(cb.like(cb.upper(join.get(PersonHistory_.lastname)), String.format("%s%%", filter.getLastname().toUpperCase())));
                    }
                    if (Objects.nonNull(filter.getPatronymic())) {
                        predicates.add(cb.like(cb.upper(join.get(PersonHistory_.patronymic)),
                                               String.format("%s%%", filter.getPatronymic().toUpperCase())));
                    }
                    if (Objects.nonNull(filter.getBirthdate())) {
                        predicates.add(cb.equal(join.get(PersonHistory_.birthDate), filter.getBirthdate()));
                    }
                    if (Objects.nonNull(filter.getDocumentSeries())) {
                        predicates.add(cb.equal(join.get(PersonHistory_.document).get(BaseDocument_.series), filter.getDocumentSeries()));
                    }
                    if (Objects.nonNull(filter.getDocumentNumber())) {
                        predicates.add(cb.equal(join.get(PersonHistory_.document).get(BaseDocument_.number), filter.getDocumentNumber()));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
