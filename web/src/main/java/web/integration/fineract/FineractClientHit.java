/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class FineractClientHit implements Serializable {

    private final Long fineractClientId;

    private final String displayName;

    private final String firstname;

    private final String lastname;

    private final String middlename;

    private final String externalId;

    private final String accountNo;

    private final String mobileNo;

    private final LocalDate dateOfBirth;

    public FineractClientHit(Long fineractClientId, String displayName, String firstname, String lastname, String middlename,
                             String externalId, String accountNo, String mobileNo, LocalDate dateOfBirth) {
        this.fineractClientId = fineractClientId;
        this.displayName = displayName;
        this.firstname = firstname;
        this.lastname = lastname;
        this.middlename = middlename;
        this.externalId = externalId;
        this.accountNo = accountNo;
        this.mobileNo = mobileNo;
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatusLabel() {
        return "In Fineract, not in CRM";
    }
}
