/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.crm.validator;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class PersonValidateException extends Exception {

    @Getter
    private final List<String> details = new ArrayList<>();

    public PersonValidateException(String message) {
        super(message);
    }

    public PersonValidateException(String message, List<String> details) {
        this(message);
        this.details.addAll(details);
    }
}
