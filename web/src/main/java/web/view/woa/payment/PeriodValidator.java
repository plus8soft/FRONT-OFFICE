/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.woa.payment;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import web.utils.DateTimes;
import web.view.Message;

@Getter
@Setter
@Component
public class PeriodValidator implements Validator, Message {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.equals(((UIInput) component).getValue())) {
            String period = (String) value;
            try {
                LocalDate.parse(period, DateTimes.DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                if (period.matches("(MS|Q|HY|YR)\\.(\\d{2})\\.(\\d{4})")) {
                    String[] parts = period.split("\\.");
                    int maxYear = Year.now().getValue() + 1;
                    int periodYear = Integer.parseInt(parts[2]);
                    if (periodYear > 1991 && periodYear < maxYear) {
                        switch (parts[0]) {
                            case "MS":
                                if (!parts[1].matches("(0|1)\\d")) {
                                    throw new ValidatorException(errorMessage("Period field input error", "month is incorrectly specified"));
                                }
                                break;
                            case "Q":
                                if (!parts[1].matches("0[1-4]")) {
                                    throw new ValidatorException(errorMessage("Period field input error", "quarter is incorrectly specified"));
                                }
                                break;
                            case "HY":
                                if (!parts[1].matches("0(1|2)")) {
                                    throw new ValidatorException(errorMessage("Period field input error", "half-year is incorrectly specified"));
                                }
                                break;
                            case "YR":
                                if (!parts[1].equals("00")) {
                                    throw new ValidatorException(
                                            errorMessage("Period field input error", "For YR period, the period value must be 00"));
                                }
                                break;
                        }
                    } else {
                        throw new ValidatorException(errorMessage("Period field input error", "year is incorrectly specified"));
                    }
                } else {
                    throw new ValidatorException(errorMessage("Period field input error", "entered value is not a date or period"));
                }
            }
        }
    }
}
