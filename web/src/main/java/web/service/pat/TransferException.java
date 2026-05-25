/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class TransferException extends RuntimeException {

    private final Collection<String> messages;

    public TransferException(String message) {
        super(message);
        this.messages = Collections.singleton(message);
    }

    public TransferException(Collection<String> messages) {
        this(messages, null);
    }

    public TransferException(Collection<String> messages, Throwable cause) {
        super(messages.stream().collect(Collectors.joining("; ")), cause);
        this.messages = messages;
    }
}
