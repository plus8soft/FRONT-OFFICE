/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChangeType {
    U("Update"),
    C("Create"),
    D("Delete");

    private String value;
}
