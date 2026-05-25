/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.component;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import org.primefaces.context.RequestContext;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;

public class AutoCompleteRenderer extends org.primefaces.component.autocomplete.AutoCompleteRenderer {

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        if (submittedValue == null || "null".equals(submittedValue)) {
            return submittedValue;
        }
        ValueConverter<Object, Object> valueConverter = ((AutoComplete) component).getValueConverter();
        Object convertedValue;
        if (valueConverter == null) {
            convertedValue = super.getConvertedValue(context, component, submittedValue);
        } else {
            convertedValue = super.getConvertedValue(context, component, submittedValue);
            if (convertedValue == null) {
                convertedValue = valueConverter.toTarget((String) submittedValue);
            } else {
                convertedValue = valueConverter.toTarget(convertedValue);
            }
        }
        return convertedValue;
    }

    @Override
    protected void encodeHiddenInput(FacesContext context, org.primefaces.component.autocomplete.AutoComplete ac, String clientId)
            throws IOException {
        ValueConverter valueConverter = ((AutoComplete) ac).getValueConverter();
        if (valueConverter == null) {
            super.encodeHiddenInput(context, ac, clientId);
        } else {
            ResponseWriter writer = context.getResponseWriter();
            Object source = valueConverter.getSource();
            String valueToRender;
            if (source != null) {
                valueToRender = ac.getConverter().getAsString(context, ac, source);
            } else {
                valueToRender = valueConverter.toString(ac.getValue());
            }
            writer.startElement("input", null);
            writer.writeAttribute("id", clientId + "_hinput", null);
            writer.writeAttribute("name", clientId + "_hinput", null);
            writer.writeAttribute("type", "hidden", null);
            writer.writeAttribute("autocomplete", "off", null);
            if (valueToRender != null) {
                writer.writeAttribute("value", valueToRender, null);
            }
            writer.endElement("input");
        }
    }

    @Override
    protected void encodeInput(FacesContext context, org.primefaces.component.autocomplete.AutoComplete ac, String clientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        boolean disabled = ac.isDisabled();
        String itemLabel;
        String defaultStyleClass = ac.isDropdown() ? org.primefaces.component.autocomplete.AutoComplete.INPUT_WITH_DROPDOWN_CLASS :
                                   org.primefaces.component.autocomplete.AutoComplete.INPUT_CLASS;
        String styleClass = disabled ? defaultStyleClass + " ui-state-disabled" : defaultStyleClass;
        styleClass = ac.isValid() ? styleClass : styleClass + " ui-state-error";
        String inputStyleClass = ac.getInputStyleClass();
        inputStyleClass = (inputStyleClass == null) ? styleClass : styleClass + " " + inputStyleClass;
        writer.startElement("input", null);
        writer.writeAttribute("id", clientId + "_input", null);
        writer.writeAttribute("name", clientId + "_input", null);
        writer.writeAttribute("type", ac.getType(), null);
        writer.writeAttribute("class", inputStyleClass, null);
        writer.writeAttribute("autocomplete", "off", null);
        String labelledBy = ac.getLabelledBy();
        if (labelledBy != null) {
            writer.writeAttribute("aria-labelledby", labelledBy, null);
        }
        String inputStyle = ac.getInputStyle();
        if (inputStyle != null) {
            writer.writeAttribute("style", inputStyle, null);
        }
        renderPassThruAttributes(context, ac, HTML.INPUT_TEXT_ATTRS_WITHOUT_EVENTS);
        renderDomEvents(context, ac, HTML.INPUT_TEXT_EVENTS);
        String var = ac.getVar();
        if (var == null) {
            itemLabel = ComponentUtils.getValueToRender(context, ac);
            if (itemLabel != null) {
                writer.writeAttribute("value", itemLabel, null);
            }
        } else {
            Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
            if (ac.isValid()) {
                ValueConverter<Object, Object> valueConverter = ((AutoComplete) ac).getValueConverter();
                Object value = ac.getValue();
                if (valueConverter != null && valueConverter.getSource() == null) {
                    itemLabel = value == null ? null : valueConverter.toString(value);
                } else {
                    requestMap.put(var, ac.getValue());
                    itemLabel = ac.getItemLabel();
                }
            } else {
                Object submittedValue = ac.getSubmittedValue();
                Object value = ac.getValue();
                if (submittedValue == null && value != null) {
                    requestMap.put(var, value);
                    itemLabel = ac.getItemLabel();
                } else if (submittedValue != null) {
                    try {
                        Object item = getConvertedValue(context, ac, String.valueOf(submittedValue));
                        requestMap.put(var, item);
                        itemLabel = ac.getItemLabel();
                    } catch (ConverterException ce) {
                        itemLabel = String.valueOf(submittedValue);
                    }
                } else {
                    itemLabel = null;
                }
            }
            if (itemLabel != null) {
                writer.writeAttribute("value", itemLabel, null);
            }
            requestMap.remove(var);
        }
        if (disabled) {
            writer.writeAttribute("disabled", "disabled", null);
        }
        if (ac.isReadonly()) {
            writer.writeAttribute("readonly", "readonly", null);
        }
        if (ac.isRequired()) {
            writer.writeAttribute("aria-required", "true", null);
        }
        if (RequestContext.getCurrentInstance().getApplicationContext().getConfig().isClientSideValidationEnabled()) {
            renderValidationMetadata(context, ac);
        }
        writer.endElement("input");
    }
}
