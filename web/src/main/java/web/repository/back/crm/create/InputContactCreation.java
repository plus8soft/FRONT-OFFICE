/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@AllArgsConstructor
@Table(name = "fr_pClientCreateF_CrtContact_in", index = "XPKfr_pClientCreateF_CrtContact_in")
@Data
public class InputContactCreation {

    @Column(name = "ContactValue")
    private String data;

    @Column(name = "ContactType")
    private String type;
}
