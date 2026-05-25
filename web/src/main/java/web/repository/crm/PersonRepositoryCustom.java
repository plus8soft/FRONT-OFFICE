/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.crm;

import java.util.List;
import web.entity.crm.Document;
import web.projection.PersonAutoComplete;

public interface PersonRepositoryCustom {

    List<PersonAutoComplete> findByLikeDocument(Document document);
}
