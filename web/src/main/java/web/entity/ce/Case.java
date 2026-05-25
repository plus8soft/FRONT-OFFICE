/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce;

import java.io.Serializable;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Embeddable
@Getter
@Setter
public class Case implements Serializable {

    @Formula("0")
    private int dummy;

    private String nominative;

    private String genitive;

    private String plural;
}
