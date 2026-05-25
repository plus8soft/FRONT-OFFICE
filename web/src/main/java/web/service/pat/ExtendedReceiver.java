/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedReceiver extends Receiver {

    private String citizenshipCode;

    private String citizenship;

    private Boolean nonRus;
}
