/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs;

import java.io.ByteArrayInputStream;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.util.MimeTypeUtils;

public interface XmlStreamedContentProducer {

    String generateXml();

    default StreamedContent fileDownload(String fileName) {
        return new DefaultStreamedContent(new ByteArrayInputStream(generateXml().getBytes()), MimeTypeUtils.TEXT_XML_VALUE, fileName);
    }
}
