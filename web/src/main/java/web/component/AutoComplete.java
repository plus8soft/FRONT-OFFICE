/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.component;

public class AutoComplete extends org.primefaces.component.autocomplete.AutoComplete {

    private ValueConverter<Object, Object> valueConverter;

    public ValueConverter<Object, Object> getValueConverter() {
        if (this.valueConverter != null) {
            return (this.valueConverter);
        }
        return (ValueConverter) getStateHelper().eval("valueConverter");
    }

    public void setValueConverter(ValueConverter<Object, Object> valueConverter) {
        clearInitialState();
        this.valueConverter = valueConverter;
    }

    @Override
    public Object getLocalValue() {
        Object localValue = super.getLocalValue();
        return "null".equals(localValue) ? null : localValue;
    }

    @Override
    public String getItemLabel() {
        return super.getItemLabel();
    }
}
