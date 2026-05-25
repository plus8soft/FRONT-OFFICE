/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service;

import java.time.Instant;
import lombok.Getter;
import web.entity.AuthorizationResult;
import web.entity.core.User;

public class AuthorizationException extends Exception {

    @Getter
    private final AuthorizationResult authorizationResult;

    @Getter
    private final User user;

    @Getter
    private final Instant date;

    public AuthorizationException(AuthorizationResult authorizationResult, Instant date) {
        this(authorizationResult, null, date);
    }

    public AuthorizationException(AuthorizationResult authorizationResult, User user, Instant date) {
        super(authorizationResult.getMessage());
        this.authorizationResult = authorizationResult;
        this.user = user;
        this.date = date;
    }
}
