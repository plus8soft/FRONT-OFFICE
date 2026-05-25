/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.system;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.PaymentSystem;
import web.entity.dict.PaymentSystemName;
import web.repository.dict.PaymentSystemRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class PaymentSystemModel extends AbstractVirtualScrollLazyModel<PaymentSystem, PaymentSystemName> {

    @Autowired
    private PaymentSystemRepository paymentSystemRepository;

    private PaymentSystem selected;

    @Override
    protected Function<PaymentSystem, PaymentSystemName> keyFunction() {
        return PaymentSystem::getId;
    }

    @Override
    protected long count() {
        return paymentSystemRepository.count();
    }

    @Override
    public List<PaymentSystem> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return paymentSystemRepository.findAll(null, first, pageSize);
    }
}
