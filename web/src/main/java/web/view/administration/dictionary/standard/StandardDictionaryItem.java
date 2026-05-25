/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import web.entity.core.Dictionary;

@Getter
@Setter
public class StandardDictionaryItem implements Serializable {

    private Dictionary dictionary;

    private long count;
}
