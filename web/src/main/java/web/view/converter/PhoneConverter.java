/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("Phone")
public class PhoneConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return value == null ? null : value.replaceAll("[()-]", "");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? null : ((String) value).replaceFirst("(?<=\\+7)(\\d{1,3})", "($1)").replaceAll("(?<=\\)|\\d{3})(\\d{1,2}+)", "-$1");
    }
}
