/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import web.entity.crm.Person;
import web.view.Message;

@Component
public class DepositInfoCache implements Message {

    @Autowired
    private PersonBackService personBackService;

    @Cacheable(value = "depositsInfo", key = "#person.id", sync = true)
    public DepositInfoWrapper loadDepositInfo(String userName, Person person) {
        return personBackService.findDeposits(userName, person);
    }

    @CacheEvict(value = "depositsInfo", key = "#person.id", beforeInvocation = true)
    public void resetCache(Person person) {
    }
}
