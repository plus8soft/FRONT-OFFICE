/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.get;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.core.DepartmentPaymentSystem;
import web.entity.core.DepartmentPaymentSystem_;
import static web.entity.dict.PaymentSystemName.MONEYGRAM;
import static web.entity.dict.PaymentSystemName.MONEY_TRANSFER;
import static web.entity.dict.PaymentSystemName.WESTERN_UNION;
import web.entity.dict.PaymentSystem_;
import web.repository.core.DepartmentPaymentSystemRepository;
import web.session.UserSession;

@Getter
@Setter
public class StepOneView implements Serializable {

    @Autowired
    private DepartmentPaymentSystemRepository departmentPaymentSystemRepository;

    @Autowired
    private UserSession userSession;

    private List<DepartmentPaymentSystem> departmentPaymentSystems;

    private AbstractPayoutTransfer payoutTransfer;

    public void init() {
        departmentPaymentSystems = departmentPaymentSystemRepository.findAll((root, query, cb) -> {
            root.fetch(DepartmentPaymentSystem_.paymentSystem);
            return cb.and(cb.equal(root.get(DepartmentPaymentSystem_.department), userSession.getUser().getDepartment()),
                          cb.isTrue(root.get(DepartmentPaymentSystem_.payOut)),
                          cb.isTrue(root.get(DepartmentPaymentSystem_.paymentSystem).get(PaymentSystem_.enabled)));
        });
    }

    public String next(DepartmentPaymentSystem departmentPaymentSystem) {
        switch (departmentPaymentSystem.getPaymentSystem().getId()) {
            case MONEY_TRANSFER:
            case WESTERN_UNION:
            case MONEYGRAM:
                payoutTransfer = new web.view.transfer.get.PayoutTransfer();
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment system: " + departmentPaymentSystem.getPaymentSystem().getId());
        }
        payoutTransfer.setPaymentSystem(departmentPaymentSystem.getPaymentSystem());
        payoutTransfer.setDepartmentCode(departmentPaymentSystem.getCode());
        return "next";
    }
}
