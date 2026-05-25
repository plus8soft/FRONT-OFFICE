/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view;

import java.io.Serializable;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.AuthorizationResult;
import web.service.AuthorizationException;
import web.service.AuthorizationInfo;
import web.service.AuthorizationService;
import web.session.UserSession;

@Getter
@Setter
@Log4j2
public class AuthorizationView implements Message, Serializable {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private UserSession userSession;

    private String login;

    private String password;

    private String signedContent = String.format("%04X", 0xFFFF & (int) (Math.random() * 0xFFFF));

    public String authorizeViaToken() throws AuthorizationException {
        userSession.setViaToken(true);
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return authorize(
                () -> authorizationService.login(requestParameterMap.get("serialNumber"), signedContent, requestParameterMap.get("signature")));
    }

    public String authorize() throws AuthorizationException {
        userSession.setViaToken(false);
        return authorize(() -> authorizationService.login(login, password));
    }

    private String authorize(AuthorizationInfoSupplier authorizationInfoSupplier) throws AuthorizationException {
        String action = null;
        try {
            AuthorizationInfo authorizationInfo = authorizationInfoSupplier.authorize();
            userSession.setConnectionEvent(authorizationInfo.getConnectionEvent());
            userSession.setUser(authorizationInfo.getUser());
            userSession.setDepartmentsGraph(authorizationInfo.getDepartmentsGraph());
            userSession.setDepartments(authorizationInfo.getDepartments());
            userSession.setTasks(authorizationInfo.getTasks());
            userSession.setRights(authorizationInfo.getRights());
            ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false))
                    .setMaxInactiveInterval(userSession.getUser().getSecurityProfile().getSessionTimeout() * 60);
            action = "next";
        } catch (AuthorizationException e) {
            if (AuthorizationResult.REQUIRE_CHANGE_PASSWORD.equals(e.getAuthorizationResult()) ||
                AuthorizationResult.EXPIRED_PASSWORD.equals(e.getAuthorizationResult())) {
                throw e;
            }
            addErrorMessage(AuthorizationResult.WRONG_LOGIN.equals(e.getAuthorizationResult()) ||
                            AuthorizationResult.WRONG_PASSWORD.equals(e.getAuthorizationResult()) ? "Invalid login or password" : e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Authorization error");
        }
        return action;
    }

    private interface AuthorizationInfoSupplier {

        AuthorizationInfo authorize() throws AuthorizationException;
    }
}
