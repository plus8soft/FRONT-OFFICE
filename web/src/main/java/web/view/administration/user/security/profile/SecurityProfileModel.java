/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.security.profile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.core.User_;
import web.repository.core.SecurityProfileRepository;
import web.repository.core.UserRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class SecurityProfileModel extends AbstractVirtualScrollLazyModel<SecurityProfileItem, Long> {

    @Autowired
    private SecurityProfileRepository securityProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected Function<SecurityProfileItem, Long> keyFunction() {
        return securityProfileItem -> securityProfileItem.getSecurityProfile().getId();
    }

    @Override
    protected long count() {
        return securityProfileRepository.count();
    }

    @Override
    public List<SecurityProfileItem> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return securityProfileRepository.findAll().stream().map(securityProfile -> new SecurityProfileItem(securityProfile, userRepository
                .count((root, query, cb) -> cb.equal(root.get(User_.securityProfile), securityProfile)))).collect(Collectors.toList());
    }
}
