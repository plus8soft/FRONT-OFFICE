/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pChangeClientF_ModifyAlterName2_in", index = "XPKfr_pChangeClientF_ModifyAlterName2_in")
@AllArgsConstructor
@Data
public class InputAlterNameChange {

    @Column(name = "AlterNameTypeBrief")
    private String type;

    @Column(name = "SurName")
    private String lastname;

    @Column(name = "Name")
    private String firstname;

    @Column(name = "PatronymicName")
    private String patronymic;
}
