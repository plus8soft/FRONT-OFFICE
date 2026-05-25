/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.report.context.document.additional;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.service.crm.report.context.document.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalDocumentContext {

    private List<Document> documents = new ArrayList<>();
}
