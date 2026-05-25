/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.core.DepartmentPaymentSystem_;
import web.repository.core.DepartmentPaymentSystemRepository;
import web.session.UserSession;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class TransferView implements Message, Serializable {

    @Autowired
    private DepartmentPaymentSystemRepository departmentPaymentSystemRepository;


    @Autowired
    private UserSession userSession;

    private boolean send;

    private boolean payout;

    public void init() {
        send = departmentPaymentSystemRepository.exists((root, query, cb) -> cb
                .and(cb.equal(root.get(DepartmentPaymentSystem_.department), userSession.getUser().getDepartment()),
                     cb.isTrue(root.get(DepartmentPaymentSystem_.send))));
        payout = departmentPaymentSystemRepository.exists((root, query, cb) -> cb
                .and(cb.equal(root.get(DepartmentPaymentSystem_.department), userSession.getUser().getDepartment()),
                     cb.isTrue(root.get(DepartmentPaymentSystem_.payOut))));
    }

    public String send() {
        return "send";
    }

    public String get() {
        return "get";
    }
}
