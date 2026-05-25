/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.account;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.entity.dict.Currency;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccountFilter extends web.service.dict.AccountFilter implements Serializable {

    private List<Currency> currencies = new ArrayList<>();

    private Boolean status;

    private String name;

    @Override
    public AccountFilter clone() {
        return (AccountFilter) super.clone();
    }
}
