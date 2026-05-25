/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata.country;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.service.pat.transferdata.AbstractResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
public class Response extends AbstractResponse {

    private List<Country> countries;
}
