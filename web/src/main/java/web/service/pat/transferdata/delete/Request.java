/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata.delete;

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

    private String number;

    public Request(String departmentCode, String number) {
        setId(departmentCode);
        this.number = number;
    }
}
