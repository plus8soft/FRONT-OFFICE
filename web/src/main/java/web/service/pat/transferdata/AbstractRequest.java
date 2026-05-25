/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata;

import java.time.Instant;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import web.jaxb.InstantAdapter;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractRequest {

    private UUID uuid = UUID.randomUUID();

    private String id;

    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant time = Instant.now();
}
