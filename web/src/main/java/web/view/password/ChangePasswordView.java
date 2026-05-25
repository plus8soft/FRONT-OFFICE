/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.password;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.AuthorizationResult;
import web.entity.core.PasswordComplexity;
import web.entity.core.User;
import web.service.AuthorizationService;

@Getter
@Setter
public class ChangePasswordView implements Serializable {

    private static final String LOGOUT = "logout";

    private static final String CANCEL = "cancel";

    @Autowired
    private AuthorizationService authorizationService;

    private User user;

    private String password;

    private String regex;

    private AuthorizationResult authorizationResult;

    public void init() {
        PasswordComplexity passwordComplexity = user.getSecurityProfile().getPasswordComplexity();
        regex = "(" + Stream.of(passwordComplexity.isRequireMixedRegister() ? "(?=.*[a-z])(?=.*[A-Z])" : null,
                                passwordComplexity.isRequireNumbers() ? "(?=.*\\d)" : null,
                                passwordComplexity.isRequireSpecialCharacters() ? "(?=.*[!@#$%^&*?_~.,;=+-])" : null,
                                passwordComplexity.isForbidUserData() ? "(?!(?i:.*" + user.getLogin() + "))" : null).filter(Objects::nonNull)
                            .collect(Collectors.joining()) + ".+)";
    }

    public String change() {
        authorizationService.changePassword(user, password);
        return authorizationResult == null ? LOGOUT : CANCEL;
    }
}
