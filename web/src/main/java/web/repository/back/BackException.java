/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back;

import lombok.Getter;

public class BackException extends RuntimeException {

    public static final BackException UNKNOWN = new BackException("Error communicating with core banking system");

    @Getter
    private final int code;

    public BackException(int code, String message) {
        this(code, message, null);
    }

    public BackException(String message) {
        this(0, message, null);
    }

    public BackException(String message, Throwable cause) {
        this(0, message, cause);
    }

    public BackException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
