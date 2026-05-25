/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.item;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.ce.Rule;
import web.entity.ce.RuleParameter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleItem implements Serializable {

    private Rule rule;

    private RuleParameter ruleParameter;

    private boolean enabledConflict;

    private boolean sellConflict;

    private boolean buyConflict;
}
