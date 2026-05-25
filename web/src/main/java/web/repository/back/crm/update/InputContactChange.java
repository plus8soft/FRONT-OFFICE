/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pChangeClientF_ModifyContact2_in", index = "XPKfr_pChangeClientF_ModifyContact2_in")
@AllArgsConstructor
@Data
public class InputContactChange {

    @Column(name = "ContactTypeBrief")
    private String type;

    @Column(name = "ContactValue")
    private String value;

    @Column(name = "ContactValue_old")
    private String oldValue;
}
