/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import org.springframework.stereotype.Repository;
import web.entity.dict.Region;
import web.repository.CustomJpaRepository;

@Repository(value = "dict.RegionRepository")
public interface RegionRepository extends CustomJpaRepository<Region, Long> {

}
