/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientCreateF_CrtAlterName_in", index = "XPKfr_pClientCreateF_CrtAlterName_in")
@AllArgsConstructor
@Data
public class InputAlterNameCreation {

    @Column(name = "TypeName")
    private String type;

    @Column(name = "SurName")
    private String lastName;

    @Column(name = "PatronymicName")
    private String patronymic;

    @Column(name = "Name")
    private String firstname;
}
