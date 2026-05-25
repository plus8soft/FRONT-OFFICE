/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

public class FineractException extends RuntimeException {

    public FineractException(String message) {
        super(message);
    }

    public FineractException(String message, Throwable cause) {
        super(message, cause);
    }
}
