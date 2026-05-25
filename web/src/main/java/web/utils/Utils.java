/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.utils;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Utils {

    private Addresses addresses = new Addresses();

    private Strings strings = new Strings();

    private SumTranslator sumTranslator = new SumTranslator();
}
