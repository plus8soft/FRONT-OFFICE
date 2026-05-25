/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard.elements;

import java.io.Serializable;
import lombok.Data;
import web.entity.core.Dictionary;

@Data
public class DictionaryValueFilter implements Serializable, Cloneable {

    private String textFilter;

    private Dictionary dictionary;

    @Override
    public DictionaryValueFilter clone() {
        try {
            return (DictionaryValueFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
