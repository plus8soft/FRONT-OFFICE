/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.dict.Currency;
import web.repository.CustomJpaRepository;

public interface CurrencyRepository extends CustomJpaRepository<Currency, String> {

    @Query("SELECT a.image FROM Currency a WHERE a.id = :id")
    byte[] findImageById(@Param("id") String id);

    @Query("SELECT MAX(c.position) FROM Currency c")
    Optional<Integer> findLastPosition();

    Currency findByIso(String iso);
}
