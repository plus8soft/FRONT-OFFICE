/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import web.dictionary.EventCodeDictionary;
import web.dictionary.EventStatusDictionary;
import web.entity.core.EventSetting;
import web.entity.core.EventSetting_;
import web.entity.log.AbstractEvent;
import web.repository.core.EventSettingRepository;
import web.session.UserSession;
import web.view.administration.auditlogs.XmlStreamedContentProducer;

@Getter
@Setter
public abstract class AbstractEventShowView<T extends AbstractEvent> implements Serializable, XmlStreamedContentProducer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final DateTimeFormatter FORMATTER_FOR_FILE_NAME = DateTimeFormatter.ofPattern("ddMMyyyy_HHmm");

    @Autowired
    private EventCodeDictionary eventCodeDictionary;

    @Autowired
    private EventStatusDictionary eventStatusDictionary;

    @Autowired
    private EventSettingRepository eventSettingRepository;

    @Autowired
    private UserSession userSession;

    private T event;

    private EventSetting eventSetting;

    public abstract String getTitleXmlFile();

    public abstract String getPrefixNameFileXml();

    public abstract String getHeaderView();

    public void init(T event) {
        this.event = event;
        this.eventSetting = eventSettingRepository.findOne((root, query, cb) -> cb.equal(root.get(EventSetting_.code), event.getCode()));
    }

    @Override
    public String generateXml() {
        StringBuilder xmlText = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xmlText.append("<Event_Details>");
        xmlText.append("<Title>").append(getTitleXmlFile()).append("</Title>");
        xmlText.append("<Event>");
        xmlText.append("<Date_Time>").append(Objects.nonNull(event.getDate()) ?
                                                event.getDate().atZone(userSession.getUser().getDepartment().getZoneId())
                                                     .format(DATE_TIME_FORMATTER) : "").append("</Date_Time>");
        xmlText.append("<Result>").append(eventStatusDictionary.findOne(event.getStatus()).getValue()).append("</Result>");
        xmlText.append("<Code>").append(eventCodeDictionary.findOne(event.getCode()).getValue()).append("</Code>");
        xmlText.append("<Event_Description>").append(eventSetting.getDescription()).append("</Event_Description>");
        xmlText.append("<Additional_Info>").append(event.getDescription()).append("</Additional_Info>");
        xmlText.append("<User>");
        xmlText.append("<Lastname>").append(event.getUser().getLastname()).append("</Lastname>");
        xmlText.append("<Firstname>").append(event.getUser().getFirstname()).append("</Firstname>");
        xmlText.append("<Patronymic>").append(event.getUser().getPatronymic()).append("</Patronymic>");
        xmlText.append("<Login>").append(event.getUser().getLogin()).append("</Login>");
        xmlText.append("</User>");
        xmlText.append("<Department>").append(event.getDepartment().getName()).append("</Department>");
        xmlText.append("<Project>").append(event.getProject().getName()).append("</Project>");
        xmlText.append("<Task>").append(event.getTask().getName()).append("</Task>");
        xmlText.append("<Connection_ID>").append(event.getConnectionEvent().getId()).append("</Connection_ID>");
        return xmlText.append("</Event>").append("</Event_Details>").toString();
    }

    public StreamedContent fileDownload() {
        return fileDownload(
                LocalDateTime.now(userSession.getUser().getDepartment().getZoneId()).format(FORMATTER_FOR_FILE_NAME) + getPrefixNameFileXml());
    }
}
