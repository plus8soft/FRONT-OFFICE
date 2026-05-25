/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UpdateResult {
    SUCCESSFULLY("Successfully updated"),
    ERROR("Update error"),
    ABORTED("Update aborted");

    private String value;
}
