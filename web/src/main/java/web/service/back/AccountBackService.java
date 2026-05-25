/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import web.entity.core.Department;
import web.entity.dict.Account;
import web.repository.back.BackException;

/**
 * Stub back-service for cash / settlement account lookup against the core.
 *
 * <p>Throws {@link BackException} with {@link BackIntegrationMessages#CORE_NOT_CONNECTED}.
 * Replace the body with a real call to your core's account-listing API.
 */
@Service
@Log4j2
public class AccountBackService {

    public List<Account> findAccounts(String userName, Department department, String accountNumber) {
        throw new BackException(BackIntegrationMessages.CORE_NOT_CONNECTED);
    }
}
