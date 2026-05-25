/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.io;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.AuthorizationResult;
import web.entity.core.User;
import web.entity.log.ConnectionEvent;
import web.repository.log.ConnectionEventRepository;
import web.session.UserSession;
import web.utils.DateTimes;
import web.view.Message;
import web.view.administration.auditlogs.XmlStreamedContentProducer;

@Getter
@Setter
@Log4j2
public class IoView implements Message, Serializable, XmlStreamedContentProducer {

    @Autowired
    private ConnectionEventRepository connectionEventRepository;

    @Autowired
    private UserSession userSession;

    private ConnectionEventModel model;

    private ConnectionEventFilter filter;

    @Transactional
    public void init(ConnectionEventModel connectionEventModel) {
        model = connectionEventModel;
        model.setFilter(filter.clone());
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:connectionEvents")).reset();
    }

    @Override
    public String generateXml() {
        StringBuilder xmlText = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xmlText.append("<Login_Event>");
        xmlText.append("<Title>").append("Login_Logout_Events_List").append("</Title>");
        xmlText.append(generateXmlFilter());
        xmlText.append("<Events>");
        if (!model.getConnectionEventList().isEmpty()) {
            model.getConnectionEventList().forEach(event -> xmlText.append(generateXmlModel(event)));
        }
        return xmlText.append("</Events>").append("</Login_Event>").toString();
    }

    private String generateXmlModel(ConnectionEvent connectionEvent) {
        StringBuilder xmlText = new StringBuilder("<Event>");
        xmlText.append("<Login>").append(connectionEvent.getLogin()).append("</Login>");
        if (Objects.nonNull(connectionEvent.getUser())) {
            User user = connectionEvent.getUser();
            xmlText.append("<Lastname>").append(user.getLastname()).append("</Lastname>");
            xmlText.append("<Firstname>").append(user.getFirstname()).append("</Firstname>");
            xmlText.append("<Patronymic>").append(user.getPatronymic()).append("</Patronymic>");
        }
        xmlText.append("<Event_Date>").append(connectionEvent.getDate().atZone(userSession.getUser().getDepartment().getZoneId()).toLocalDateTime()
                                                               .format(DateTimes.DATE_TIME_FORMATTER)).append("</Event_Date>");
        xmlText.append("<Result>")
               .append(AuthorizationResult.SUCCESS.equals(connectionEvent.getAuthorizationResult()) ? "Successful login" : "Login failed")
               .append("</Result>");
        xmlText.append("<Code>").append(connectionEvent.getAuthorizationResult().getMessage()).append("</Code>");
        xmlText.append("<IP>").append(connectionEvent.getIp()).append("</IP>");
        xmlText.append("<Logout_Date>").append(Objects.nonNull(connectionEvent.getLogoffDate()) ?
                                               connectionEvent.getLogoffDate().atZone(userSession.getUser().getDepartment().getZoneId())
                                                              .toLocalDateTime().format(DateTimes.DATE_TIME_FORMATTER) : "").append("</Logout_Date>");
        return xmlText.append("</Event>").toString();
    }

    private String generateXmlFilter() {
        StringBuilder xmlText = new StringBuilder();
        xmlText.append("<Filter_Parameters>");
        if (Objects.nonNull(filter.getFirstname())) {
            xmlText.append("<Firstname>").append(filter.getFirstname()).append("</Firstname>");
        }
        if (Objects.nonNull(filter.getLastname())) {
            xmlText.append("<Lastname>").append(filter.getLastname()).append("</Lastname>");
        }
        if (Objects.nonNull(filter.getPatronymic())) {
            xmlText.append("<Patronymic>").append(filter.getPatronymic()).append("</Patronymic>");
        }
        if (Objects.nonNull(filter.getUserLogin())) {
            xmlText.append("<User_Login>").append(filter.getUserLogin()).append("</User_Login>");
        }
        if (Objects.nonNull(filter.getUserIp())) {
            xmlText.append("<IP>").append(filter.getUserIp()).append("</IP>");
        }
        if (Objects.nonNull(filter.getResult())) {
            xmlText.append("<Result>").append(filter.getResult() ? "Successful login" : "Login failed").append("</Result>");
        }
        if (!filter.getAuthorizationResults().isEmpty()) {
            xmlText.append("<Codes>");
            filter.getAuthorizationResults().forEach(type -> xmlText.append("<Code>").append(type.getMessage()).append("</Code>"));
            xmlText.append("</Codes>");
        }
        if (Objects.nonNull(filter.getEventDateWith())) {
            xmlText.append("<Event_Date_From>")
                   .append(filter.getEventDateWith().atZone(userSession.getUser().getDepartment().getZoneId()).toLocalDateTime()
                                 .format(DateTimes.DATE_TIME_FORMATTER)).append("</Event_Date_From>");
        }
        if (Objects.nonNull(filter.getEventDate())) {
            xmlText.append("<Event_Date_To>")
                   .append(filter.getEventDate().atZone(userSession.getUser().getDepartment().getZoneId()).toLocalDateTime()
                                 .format(DateTimes.DATE_TIME_FORMATTER)).append("</Event_Date_To>");
        }
        if (Objects.nonNull(filter.getOutDateWith())) {
            xmlText.append("<Logout_Date_From>")
                   .append(filter.getOutDateWith().atZone(userSession.getUser().getDepartment().getZoneId()).toLocalDateTime()
                                 .format(DateTimes.DATE_TIME_FORMATTER)).append("</Logout_Date_From>");
        }
        if (Objects.nonNull(filter.getOutDate())) {
            xmlText.append("<Logout_Date_To>").append(filter.getOutDate().atZone(userSession.getUser().getDepartment().getZoneId()).toLocalDateTime()
                                                            .format(DateTimes.DATE_TIME_FORMATTER)).append("</Logout_Date_To>");
        }
        return xmlText.append("</Filter_Parameters>").toString();
    }

    public StreamedContent fileDownload() {
        model.loadAllConnectionEvents();
        return fileDownload(
                LocalDateTime.now(userSession.getUser().getDepartment().getZoneId()).format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm")) +
                "_connection_events.xml");
    }
}
