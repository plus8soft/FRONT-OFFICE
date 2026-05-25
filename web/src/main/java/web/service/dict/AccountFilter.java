/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.entity.core.Department;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountFilter implements Serializable, Cloneable {

    private Department department;

    private String numberAccount;

    @Override
    public AccountFilter clone() {
        try {
            return (AccountFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
