/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import web.entity.ce.Sign;

@Converter(autoApply = true)
public class SignConverter implements AttributeConverter<Sign, String> {

    private static final String PLUS = "+";

    private static final String MINUS = "-";

    @Override
    public String convertToDatabaseColumn(Sign attribute) {
        return Sign.PLUS.equals(attribute) ? PLUS : MINUS;
    }

    @Override
    public Sign convertToEntityAttribute(String dbData) {
        return PLUS.equals(dbData) ? Sign.PLUS : Sign.MINUS;
    }
}
