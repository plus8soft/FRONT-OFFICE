/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.rate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.jpa.domain.Specification;
import web.entity.ce.Order;
import web.entity.ce.Order_;
import web.entity.core.Department;
import web.repository.ce.OrderRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Configurable
public class DealModel extends AbstractVirtualScrollLazyModel<Order, Long> {

    @Autowired
    private OrderRepository orderRepository;

    private Department department;

    public DealModel(Department department) {
        this.department = department;
    }

    @Override
    protected Function<Order, Long> keyFunction() {
        return Order::getId;
    }

    @Override
    protected long count() {
        return orderRepository.count(getSpecification());
    }

    @Override
    public List<Order> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return orderRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<Order> getSpecification() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get(Order_.id.getName())));
            return cb.and(cb.equal(root.get(Order_.department), department), cb.isNotNull(root.get(Order_.dealUser)));
        };
    }
}
