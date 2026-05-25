/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.repository.back.crm.deposit.OutputDepositInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositInfoWrapper implements Serializable {

    private String errorMessage;

    private Instant creationDateTime;

    private List<OutputDepositInfo> deposits;
}
