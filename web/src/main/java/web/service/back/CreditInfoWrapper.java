/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import web.repository.back.crm.credit.OutputCreditInfo;

@Getter
@Setter
public class CreditInfoWrapper {

    private String errorMessage;

    private Instant creationDateTime;

    private List<OutputCreditInfo> credits;
}
