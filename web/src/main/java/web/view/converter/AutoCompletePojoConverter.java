/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.converter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import lombok.Getter;

public class AutoCompletePojoConverter<T> implements Converter, Serializable {

    @Getter
    private Collection<T> source;

    private Map<String, T> index;

    private Function<T, String> keySupplier;

    public AutoCompletePojoConverter(Collection<T> source, Function<T, String> keySupplier) {
        this.source = source;
        this.keySupplier = keySupplier;
        this.index = source.stream().collect(Collectors.toMap(this.keySupplier, Function.identity()));
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return value == null ? null : index.get(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? null : keySupplier.apply((T) value);
    }

    public void setSource(Collection<T> source) {
        this.source = source;
        this.index = source.stream().collect(Collectors.toMap(this.keySupplier, Function.identity()));
    }
}
