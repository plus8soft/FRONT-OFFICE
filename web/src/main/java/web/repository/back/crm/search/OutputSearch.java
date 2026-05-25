/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.search;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutputSearch {

    private OutputPersonSearch person;

    private List<OutputAddressSearch> addresses;

    private List<OutputDocumentSearch> documents;

    private List<OutputAlterNameSearch> alterNames;

    private List<OutputContactSearch> contacts;
}
