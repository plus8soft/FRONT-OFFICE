/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.banking.bank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BankFilter implements Serializable, Cloneable {

    private String name;

    private String correspondentAccount;

    private String routingNumber;

    @Override
    public BankFilter clone() {
        try {
            return (BankFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
