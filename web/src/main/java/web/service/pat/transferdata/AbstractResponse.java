/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractResponse {

    private String description;
}
