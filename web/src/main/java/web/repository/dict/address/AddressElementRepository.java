/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict.address;

import org.springframework.data.repository.NoRepositoryBean;
import web.entity.dict.address.AbstractAddressElement;
import web.repository.CustomJpaRepository;

@NoRepositoryBean
public interface AddressElementRepository<T extends AbstractAddressElement> extends CustomJpaRepository<T, String> {

}
