/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.request;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import web.service.report.ReportService;

@Getter
@Setter
@Component
@Scope("request")
public class ReportRequest {

    @Autowired
    private ReportService reportService;

    private StreamedContent report;

    @PostConstruct
    public void init() throws URISyntaxException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            report = new DefaultStreamedContent();
        } else {
            String path = facesContext.getExternalContext().getRequestParameterMap().get("path");
            report = new DefaultStreamedContent(reportService.getReport(path), "application/pdf");
        }
    }
}
