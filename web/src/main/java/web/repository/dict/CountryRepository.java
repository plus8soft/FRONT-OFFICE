/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.entity.dict.Country;
import web.repository.CustomJpaRepository;

public interface CountryRepository extends CustomJpaRepository<Country, String> {

    @Query("SELECT a.alpha3 FROM Country a WHERE a.id = :id")
    String findAlpha3ById(@Param("id") String id);

    @Query("SELECT a.alpha2 FROM Country a WHERE a.id = :id")
    String findAlpha2ById(@Param("id") String id);

    Country findByAlpha3(String alpha3);

    Country findByAlpha2(String alpha2);

    List<Country> findAllByOrderByNameAsc();
}
