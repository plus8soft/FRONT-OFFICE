/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata.transfer;

import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import web.service.pat.transferdata.AbstractTransferResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
public class Response extends AbstractTransferResponse {

}
