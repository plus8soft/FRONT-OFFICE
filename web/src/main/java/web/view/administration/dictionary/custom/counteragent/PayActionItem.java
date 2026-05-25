/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.entity.dict.CounteragentPayAction;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "uuid")
public class PayActionItem implements Serializable {

    private final UUID uuid = UUID.randomUUID();

    private CounteragentPayAction payAction;
}
