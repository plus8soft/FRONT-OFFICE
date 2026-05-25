/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.component;

public interface ValueConverter<S, T> {

    S getSource();

    String toString(T target);

    T toTarget(S source);

    T toTarget(String source);
}
