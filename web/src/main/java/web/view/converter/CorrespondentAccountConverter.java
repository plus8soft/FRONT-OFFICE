/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("CorrespondentAccount")
public class CorrespondentAccountConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? null :
               ((String) value).replaceAll("((?<=^\\d{5})\\d{1,3}+|(?<=^\\d{8})\\d|(?<=^\\d{9})\\d{1,4}+|(?<=^\\d{13})\\d{1,7}+)", " - $1");
    }
}
