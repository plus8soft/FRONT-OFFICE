/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthorizationResult {
    SUCCESS("User successfully logged in", true),
    UNKNOWN_ERROR("Login denied (internal error)", false),
    WRONG_LOGIN("Login denied (unknown username)", false),
    WRONG_PASSWORD("Login denied (incorrect password)", false),
    BLOCKED("Login denied (user account is blocked)", false),
    EMPTY_AVAILABLE_TASKS("Login denied (no tasks assigned to user)", false),
    REQUIRE_CHANGE_PASSWORD("Change password on login", false),
    EXPIRED_PASSWORD("Password expired", false),
    BLOCKED_CERTIFICATE("Certificate is blocked", false),
    WRONG_CERTIFICATE_PERIOD("Certificate validity period is not current", false),
    WRONG_CERTIFICATE("Certificate is not registered in the application", false),
    WRONG_CERTIFICATE_SIGNATURE("Digital signature is invalid", false),
    BLOCKED_DEPARTMENT("Department is disabled. Contact administrator", false);

    private String message;

    private boolean success;
}
