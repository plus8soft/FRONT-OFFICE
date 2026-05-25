/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.dict;

import web.entity.dict.DbfLoadResult;
import web.repository.CustomJpaRepository;

public interface DbfLoadResultRepository extends CustomJpaRepository<DbfLoadResult, Long> {

    DbfLoadResult findTopByDictionaryNameOrderByIdDesc(String dictionaryName);
}
