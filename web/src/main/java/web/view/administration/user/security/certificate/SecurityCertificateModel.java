/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.security.certificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import web.entity.core.Certificate;
import web.entity.core.Certificate_;
import web.entity.core.User;
import web.entity.core.User_;
import web.repository.core.CertificateRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class SecurityCertificateModel extends AbstractVirtualScrollLazyModel<Certificate, Long> {

    @Autowired
    private CertificateRepository certificateRepository;

    private SecurityCertificateFilter filter;

    @Override
    protected Function<Certificate, Long> keyFunction() {
        return Certificate::getId;
    }

    @Override
    protected long count() {
        return certificateRepository.count(getSpecification());
    }

    @Override
    public List<Certificate> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return certificateRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<Certificate> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getTextFilter())) {
                Join<Certificate, User> certificateUserJoin = root.join(Certificate_.user);
                predicates.add(cb.or(
                        cb.like(cb.upper(certificateUserJoin.get(User_.firstname)), String.format("%s%%", filter.getTextFilter().toUpperCase())),
                        cb.like(cb.upper(certificateUserJoin.get(User_.lastname)), String.format("%s%%", filter.getTextFilter().toUpperCase())),
                        cb.like(cb.upper(certificateUserJoin.get(User_.patronymic)), String.format("%s%%", filter.getTextFilter().toUpperCase())),
                        cb.like(cb.upper(certificateUserJoin.get(User_.login)), String.format("%s%%", filter.getTextFilter().toUpperCase()))));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getStartDate())) {
                    predicates.add(cb.greaterThan(root.get(Certificate_.startDate), filter.getStartDate()));
                }
                if (Objects.nonNull(filter.getEndDate())) {
                    predicates.add(cb.lessThan(root.get(Certificate_.endDate), filter.getEndDate()));
                }
                if (Objects.nonNull(filter.getSerialNumber())) {
                    predicates.add(cb.like(cb.upper(root.get(Certificate_.serialNumber)),
                                           String.format("%s%%", filter.getSerialNumber().toUpperCase())));
                }
                if (Objects.nonNull(filter.getLocked())) {
                    predicates.add(cb.equal(root.get(Certificate_.locked), filter.getLocked()));
                }
                if (!filter.getUserStatuses().isEmpty() || Objects.nonNull(filter.getExpirationDate()) || !filter.getSecurityProfiles().isEmpty() ||
                    !filter.getDepartmentByNames().isEmpty() || !filter.getDepartmentByCodes().isEmpty()) {
                    Join<Certificate, User> certificateUserJoin =
                            root.getJoins().isEmpty() ? root.join(Certificate_.user) : (Join<Certificate, User>) root.getJoins().iterator().next();
                    if (!filter.getUserStatuses().isEmpty()) {
                        predicates.add(certificateUserJoin.get(User_.status).in(filter.getUserStatuses()));
                    }
                    if (Objects.nonNull(filter.getExpirationDate())) {
                        predicates.add(cb.lessThanOrEqualTo(certificateUserJoin.get(User_.accountExpirationDate), filter.getExpirationDate()));
                    }
                    if (!filter.getSecurityProfiles().isEmpty()) {
                        predicates.add(certificateUserJoin.get(User_.securityProfile).in(filter.getSecurityProfiles()));
                    }
                    if (!filter.getDepartmentByNames().isEmpty()) {
                        predicates.add(certificateUserJoin.get(User_.department).in(filter.getDepartmentByNames()));
                    }
                    if (!filter.getDepartmentByCodes().isEmpty()) {
                        predicates.add(certificateUserJoin.get(User_.department).in(filter.getDepartmentByCodes()));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
