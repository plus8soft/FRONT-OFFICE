/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.woa.payment;

import web.component.ValueConverter;
import web.entity.dict.Counteragent;

public class CounteragentEinValueConverter implements ValueConverter<Counteragent, String> {

    private Counteragent source;

    @Override
    public Counteragent getSource() {
        return source;
    }

    public void setSource(Counteragent source) {
        this.source = source;
    }

    @Override
    public String toString(String target) {
        return target;
    }

    @Override
    public String toTarget(Counteragent counteragent) {
        source = counteragent;
        return counteragent == null ? null : counteragent.getEin();
    }

    @Override
    public String toTarget(String source) {
        return source;
    }
}
