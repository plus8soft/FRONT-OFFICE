/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.utils;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class Strings {

    public String capitalizeFully(final String str) {
        return str == null ? "" :
               Stream.of(str.trim().split("\\s")).filter(s -> !s.isEmpty()).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                     .collect(Collectors.joining(" "));
    }

    public String capitalizeFio(final String lastname, final String firstname, final String patronymic) {
        return joinFio(lastname, firstname, patronymic).replaceAll("(?<=\\s\\S)\\S+", ".");
    }

    public String joinFio(final String lastname, final String firstname, final String patronymic) {
        return capitalizeFully(Stream.of(lastname, firstname, patronymic).filter(Objects::nonNull).collect(Collectors.joining(" ")));
    }

    public String join(final String delimiter, final String... values) {
        return Stream.of(values).filter(Objects::nonNull).collect(Collectors.joining(delimiter));
    }
}
