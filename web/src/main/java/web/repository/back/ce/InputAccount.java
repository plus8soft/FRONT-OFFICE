/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.ce;

import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pRestAccount_in", index = "XPKfr_pRestAccount_in")
@AllArgsConstructor
@Data
public class InputAccount {

    @Column(name = "AccountBrief")
    private String account;

    @Column(name = "InstFilID")
    private Long departmentId;
}
