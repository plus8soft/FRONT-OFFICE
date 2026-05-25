/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata.point;

import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import web.service.pat.transferdata.AbstractRequest;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@XmlRootElement
public class Request extends AbstractRequest {

    private String country;

    public Request(String departmentCode, String country) {
        setId(departmentCode);
        this.country = country;
    }
}
