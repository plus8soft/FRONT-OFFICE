/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DealStatus {
    DURING("In progress"),
    COMPLETED("Completed"),
    CANCELED("Canceled");

    private String value;
}
