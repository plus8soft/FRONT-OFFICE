/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import web.entity.crm.Person;

@Component
public class CreditInfoCache {

    @Autowired
    private PersonBackService personBackService;

    @Cacheable(value = "creditsInfo", key = "#person.id", sync = true)
    public CreditInfoWrapper loadCreditInfo(String userName, Person person) {
        return personBackService.findCredits(userName, person);
    }

    @CacheEvict(value = "creditsInfo", key = "#person.id", beforeInvocation = true)
    public void resetCache(Person person) {
    }
}
