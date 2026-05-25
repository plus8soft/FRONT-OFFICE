/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.right;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import web.entity.core.Right;
import web.entity.core.Role_;
import web.entity.core.User_;
import web.repository.core.RightRepository;
import web.repository.core.RoleRepository;
import web.repository.core.UserRepository;

public class RightModel extends LazyDataModel<RightItem> implements Serializable {

    @Autowired
    private RightRepository rightRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public RightItem getRowData(String rowKey) {
        Long id = Long.valueOf(rowKey);
        return ((List<RightItem>) getWrappedData()).stream().filter(roleItem -> id.equals(roleItem.getRight().getId())).findFirst()
                                                   .orElseGet(() -> null);
    }

    @Override
    public Object getRowKey(RightItem rightItem) {
        return rightItem.getRight().getId();
    }

    @Override
    public List<RightItem> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        Page<Right> page = rightRepository.findAll(new PageRequest(Math.round(first / pageSize), pageSize));
        setRowCount((int) page.getTotalElements());
        return page.getContent().stream()
                   .map(right -> new RightItem(right, userRepository.count((root, query, cb) -> cb.isMember(right, root.get(User_.rights))),
                                               roleRepository.count((root, query, cb) -> cb.isMember(right, root.get(Role_.rights)))))
                   .collect(Collectors.toList());
    }
}
