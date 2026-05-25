/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.audit;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.webflow.execution.RequestContextHolder;
import web.entity.core.EventCode;
import web.entity.core.EventStatus;
import web.entity.log.AbstractEvent;
import web.session.Menu;
import web.session.UserSession;

public class EventAspect {

    public static final String CS_MENU = "CS_MENU";

    public static final String CS_EVENT_GROUP = "CS_EVENT_GROUP";

    private static EventSettingCache eventSettingCache;

    @Autowired
    private ApplicationContext applicationContext;

    private Repositories repositories;

    public static boolean isEnabled(EventCode code) {
        return eventSettingCache.isEnabled(code);
    }

    @PostConstruct
    private void init() {
        repositories = new Repositories(applicationContext);
    }

    protected void startGroup() {
        RequestContextHolder.getRequestContext().getConversationScope().put(CS_EVENT_GROUP, UUID.randomUUID());
    }

    protected UUID getGroup() {
        return Optional.ofNullable((UUID) RequestContextHolder.getRequestContext().getConversationScope().get(CS_EVENT_GROUP))
                       .orElseThrow(() -> new RuntimeException("Event group identifier is not defined for this audit event."));
    }

    @SuppressWarnings("checkstyle:all")
    protected Object template(Template template, EventCode code) throws Throwable {
        Class<? extends AbstractEvent> eventClass = eventSettingCache.getType(code).getEventClass();
        AbstractEvent event = eventClass.newInstance();
        event.setCode(code);
        UserSession userSession = applicationContext.getBean(UserSession.class);
        event.setUser(userSession.getUser());
        event.setDepartment(userSession.getUser().getDepartment());
        event.setConnectionEvent(userSession.getConnectionEvent());
        event.setDate(Instant.now());
        event.setTask(Menu.getInstance().getTask());
        event.setProject(event.getTask().getProject());
        try {
            Object object = template.call(event);
            event.setStatus(event.getStatus() == null ? EventStatus.SUCCESS : event.getStatus());
            List<FacesMessage> messages = FacesContext.getCurrentInstance().getMessageList();
            String warningMessage = messages.stream().filter(facesMessage -> FacesMessage.SEVERITY_WARN.equals(facesMessage.getSeverity()))
                                            .map(facesMessage -> facesMessage.getSummary() + " " + facesMessage.getDetail())
                                            .collect(Collectors.joining(" "));
            if (!warningMessage.isEmpty()) {
                event.setStatus(EventStatus.ERROR.equals(event.getStatus()) ? event.getStatus() : EventStatus.WARNING);
                event.setDescription(String.format("%s Warning: %s", event.getDescription(), warningMessage));
            }
            String errorMessage = messages.stream().filter(facesMessage -> FacesMessage.SEVERITY_FATAL.equals(facesMessage.getSeverity()) ||
                                                                           FacesMessage.SEVERITY_ERROR.equals(facesMessage.getSeverity()))
                                          .map(facesMessage -> facesMessage.getSummary() + " " + facesMessage.getDetail())
                                          .collect(Collectors.joining(" "));
            if (!errorMessage.isEmpty()) {
                event.setStatus(EventStatus.ERROR);
                event.setDescription(String.format("%s Error: %s", event.getDescription(), errorMessage));
            }
            return object;
        } catch (Exception e) {
            event.setStatus(EventStatus.ERROR);
            event.setDescription(String.format("%s Internal error.", event.getDescription()));
            throw e;
        } finally {
            ((JpaRepository) repositories.getRepositoryFor(eventClass)).save(event);
        }
    }

    @Autowired
    public void setEventConfigurationCache(EventSettingCache eventSettingCache) {
        EventAspect.eventSettingCache = eventSettingCache;
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    public interface Template<T extends AbstractEvent> {

        Object call(T event) throws Throwable;
    }
}
