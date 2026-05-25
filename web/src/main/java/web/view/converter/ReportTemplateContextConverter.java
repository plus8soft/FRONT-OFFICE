/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import java.util.Arrays;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import web.entity.dict.ContextType;

@FacesConverter("ReportTemplateContextConverter")
public class ReportTemplateContextConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return Arrays.stream(ContextType.values()).filter(reportTemplateContext -> reportTemplateContext.name().equals(value)).findFirst()
                     .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? null : ((ContextType) value).name();
    }
}
