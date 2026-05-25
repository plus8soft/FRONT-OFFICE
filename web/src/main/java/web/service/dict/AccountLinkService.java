/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.entity.dict.Account;
import web.repository.back.BackException;
import web.repository.dict.AccountLinkRepository;
import web.repository.dict.AccountRepository;
import web.service.back.AccountBackService;

@Service
public class AccountLinkService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountLinkRepository accountLinkRepository;

    @Autowired
    private AccountBackService accountBackService;

    public List<Account> getBackAccounts(String userLogin, AccountFilter filter) throws BackException {
        return accountBackService.findAccounts(userLogin, filter.getDepartment(), filter.getNumberAccount());
    }

    @Transactional
    public void update(AccountFilter filter, List<Account> accounts) {
        accountRepository.deleteAllByDepartmentAndIdStartingWith(filter.getDepartment(), filter.getNumberAccount());
        accountRepository.save(accounts);
        accountRepository.flush();
        accountLinkRepository.save(accounts.stream().flatMap(account -> account.getAccountLinks().stream()).collect(Collectors.toList()));
    }
}
