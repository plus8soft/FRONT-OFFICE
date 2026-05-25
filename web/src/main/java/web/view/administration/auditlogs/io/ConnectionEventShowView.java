/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.io;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import web.dictionary.TimeZoneDictionary;
import web.entity.AuthorizationResult;
import web.entity.log.ConnectionEvent;
import web.session.UserSession;
import web.view.administration.auditlogs.XmlStreamedContentProducer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionEventShowView implements Serializable, XmlStreamedContentProducer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Autowired
    private TimeZoneDictionary timeZoneDictionary;

    @Autowired
    private UserSession userSession;

    private ConnectionEvent connectionEvent;

    public void init(ConnectionEvent connectionEvent) {
        this.connectionEvent = connectionEvent;
    }

    @Override
    public String generateXml() {
        StringBuilder xmlText = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xmlText.append("<Login_Event>");
        xmlText.append("<Title>").append("Detailed_Login_Logout_Event_Information").append("</Title>");
        xmlText.append("<Event>");
        xmlText.append("<Connection_ID>").append(connectionEvent.getId()).append("</Connection_ID>");
        xmlText.append("<User_ID>").append(connectionEvent.getUser().getId()).append("</User_ID>");
        xmlText.append("<Login_Name>").append(connectionEvent.getLogin()).append("</Login_Name>");
        xmlText.append("<Date_Time>").append(connectionEvent.getDate().atZone(userSession.getUser().getDepartment().getZoneId()).toLocalDateTime()
                                                               .format(DATE_TIME_FORMATTER)).append("</Date_Time>");
        xmlText.append("<Login_Result>")
               .append(AuthorizationResult.SUCCESS.equals(connectionEvent.getAuthorizationResult()) ? "Successful login" : "Login failed")
               .append("</Login_Result>");
        xmlText.append("<Code>").append(connectionEvent.getAuthorizationResult().getMessage()).append("</Code>");
        xmlText.append("<IP_Address>").append(connectionEvent.getIp()).append("</IP_Address>");
        xmlText.append("<User_Time_Zone>").append(timeZoneDictionary.findOne(connectionEvent.getZoneId()).getValue())
               .append("</User_Time_Zone>");
        xmlText.append("<User_Agent_String>").append(connectionEvent.getUserAgent()).append("</User_Agent_String>");
        xmlText.append("<Logout_Date>").append(Objects.nonNull(connectionEvent.getLogoffDate()) ?
                                                          connectionEvent.getLogoffDate().atZone(userSession.getUser().getDepartment().getZoneId())
                                                                         .toLocalDateTime().format(DATE_TIME_FORMATTER) : "")
               .append("</Logout_Date>");
        return xmlText.append("</Event>").append("</Login_Event>").toString();
    }

    public StreamedContent fileDownload() {
        return fileDownload(
                LocalDateTime.now(userSession.getUser().getDepartment().getZoneId()).format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm")) +
                "_details_connection_event.xml");
    }
}
