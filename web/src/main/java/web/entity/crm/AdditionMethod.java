/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdditionMethod {
    WEB_CAMERA("Received from Web camera"),
    FILE("Uploaded from local disk"),
    SCANNER("Received from scanner");

    private String value;
}
