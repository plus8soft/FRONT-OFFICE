/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.counteragent;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import web.entity.dict.Bank;

@Getter
@Setter
@Data
public class CounteragentFilter implements Serializable, Cloneable {

    private String ein;

    private String name;

    private String address;

    private String account;

    private Bank bank;

    private Boolean disabled;

    private Boolean hasContract;

    private boolean extendedSearch;

    @Override
    public CounteragentFilter clone() {
        try {
            return (CounteragentFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
