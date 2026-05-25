/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.component;

import lombok.Getter;

public class StringValueConverter<S> implements ValueConverter<S, String> {

    @Getter
    private S source;

    @Override
    public String toString(String target) {
        return target;
    }

    @Override
    public String toTarget(S source) {
        return source.toString();
    }

    @Override
    public String toTarget(String source) {
        return source;
    }
}
