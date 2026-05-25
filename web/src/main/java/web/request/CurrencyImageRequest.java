/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.request;

import java.io.ByteArrayInputStream;
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
import web.repository.dict.CurrencyRepository;

@Getter
@Setter
@Component
@Scope("request")
public class CurrencyImageRequest {

    @Autowired
    private CurrencyRepository currencyRepository;

    private StreamedContent image;

    @PostConstruct
    public void init() throws URISyntaxException, IOException {
        image = FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE ? new DefaultStreamedContent() :
                new DefaultStreamedContent(new ByteArrayInputStream(
                        currencyRepository.findImageById(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id"))));
    }
}
