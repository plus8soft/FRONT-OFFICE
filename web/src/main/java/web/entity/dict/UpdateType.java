/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UpdateType {
    NON_UPDATABLE("Not updatable"),
    VARIOUS("Automatic and manual"),
    WITH_OPTIONS("Parameterized update");

    private String value;
}
