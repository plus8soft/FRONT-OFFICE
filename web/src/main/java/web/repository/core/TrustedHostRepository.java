/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import web.entity.core.TrustedHost;

public interface TrustedHostRepository extends JpaRepository<TrustedHost, Long>, JpaSpecificationExecutor<TrustedHost> {

}
