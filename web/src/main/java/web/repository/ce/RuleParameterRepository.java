/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.ce;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import web.entity.ce.RuleParameter;
import web.entity.core.Department;
import web.entity.dict.Currency;

public interface RuleParameterRepository extends JpaRepository<RuleParameter, Long>, JpaSpecificationExecutor<RuleParameter> {

    void deleteAllByDepartmentInAndCurrencyIsNull(List<Department> departments);

    void deleteAllByDepartmentInAndCurrency(List<Department> departments, Currency currency);

    void deleteAllByDepartmentAndCurrency(Department department, Currency currency);
}
