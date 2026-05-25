/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FineractProductOption implements Serializable {

    private Long id;

    private String name;

    /** Display name for product pickers. */
    public String getTemplateLabel() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        return "Product #" + id;
    }
}
