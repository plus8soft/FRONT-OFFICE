/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard;

import java.io.Serializable;
import java.time.Instant;
import lombok.Data;

@Data
public class StandardDictionaryFilter implements Serializable, Cloneable {

    private String name;

    private String group;

    private Instant updateDateWith;

    private Instant updateDate;

    private String code;

    private String shortValue;

    private String value;

    private boolean extendedSearch;

    @Override
    public StandardDictionaryFilter clone() {
        try {
            return (StandardDictionaryFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
