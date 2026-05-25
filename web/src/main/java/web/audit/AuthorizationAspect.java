/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.audit;

import java.time.Instant;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import web.entity.AuthorizationResult;
import web.entity.core.User;
import web.entity.log.ConnectionEvent;
import web.repository.log.ConnectionEventRepository;
import web.service.AuthorizationException;
import web.service.AuthorizationInfo;
import web.session.UserSession;

@Aspect
@Component
public class AuthorizationAspect implements HttpSessionListener {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private ConnectionEventRepository connectionEventRepository;

    @PostConstruct
    private void init() {
        applicationContext.getServletContext().addListener(this);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("execution(* web.service.AuthorizationService.login(..))")
    public AuthorizationInfo doLogin(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        ConnectionEvent connectionEvent = new ConnectionEvent();
        try {
            connectionEvent.setLogin((String) proceedingJoinPoint.getArgs()[0]);
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            connectionEvent.setIp(request.getRemoteHost());
            connectionEvent.setUserAgent(request.getHeader("User-Agent"));
            AuthorizationInfo authorizationInfo = (AuthorizationInfo) proceedingJoinPoint.proceed();
            authorizationInfo.setConnectionEvent(connectionEvent);
            connectionEvent.setAuthorizationResult(AuthorizationResult.SUCCESS);
            connectionEvent.setDate(authorizationInfo.getDate());
            connectionEvent.setUser(authorizationInfo.getUser());
            connectionEvent.setDepartment(authorizationInfo.getUser().getDepartment());
            connectionEvent.setZoneId(authorizationInfo.getUser().getZoneId());
            return authorizationInfo;
        } catch (AuthorizationException e) {
            connectionEvent.setAuthorizationResult(e.getAuthorizationResult());
            connectionEvent.setDate(e.getDate());
            User user = e.getUser();
            if (user != null) {
                connectionEvent.setUser(user);
                connectionEvent.setDepartment(user.getDepartment());
                connectionEvent.setZoneId(user.getZoneId());
            }
            throw e;
        } catch (Exception e) {
            connectionEvent.setDate(Instant.now());
            connectionEvent.setAuthorizationResult(AuthorizationResult.UNKNOWN_ERROR);
            throw e;
        } finally {
            connectionEventRepository.save(connectionEvent);
        }
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        UserSession userSession =
                (UserSession) httpSessionEvent.getSession().getAttribute(StringUtils.uncapitalize(UserSession.class.getSimpleName()));
        if (userSession.isAuthenticated()) {
            ConnectionEvent connectionEvent = userSession.getConnectionEvent();
            connectionEvent.setLogoffDate(Instant.now());
            connectionEventRepository.save(connectionEvent);
        }
    }
}
