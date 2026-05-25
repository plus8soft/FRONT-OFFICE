/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.role;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.core.Group_;
import web.entity.core.Right_;
import web.entity.core.Task_;
import web.entity.core.User_;
import web.repository.core.GroupRepository;
import web.repository.core.RightRepository;
import web.repository.core.RoleRepository;
import web.repository.core.TaskRepository;
import web.repository.core.UserRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class RoleModel extends AbstractVirtualScrollLazyModel<RoleItem, Long> {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RightRepository rightRepository;

    @Override
    protected Function<RoleItem, Long> keyFunction() {
        return roleItem -> roleItem.getRole().getId();
    }

    @Override
    protected long count() {
        return roleRepository.count();
    }

    @Override
    public List<RoleItem> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return roleRepository.findAll(null, first, pageSize).stream()
                             .map(role -> new RoleItem(role, userRepository.count((root, query, cb) -> cb.isMember(role, root.get(User_.roles))),
                                                       groupRepository.count((root, query, cb) -> cb
                                                               .and(cb.isTrue(root.get(Group_.areUser)), cb.isMember(role, root.get(Group_.roles)))),
                                                       taskRepository.count((root, query, cb) -> cb.isMember(role, root.get(Task_.roles))),
                                                       rightRepository.count((root, query, cb) -> cb.isMember(role, root.get(Right_.roles)))))
                             .collect(Collectors.toList());
    }
}
