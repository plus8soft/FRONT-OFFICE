/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.dictionary.EventCodeDictionary;
import web.dictionary.EventStatusDictionary;
import web.entity.core.Department;
import web.entity.log.AbstractEvent;
import web.service.administration.department.DepartmentService;
import web.session.UserSession;
import web.view.Message;
import web.view.administration.auditlogs.XmlStreamedContentProducer;

@Getter
@Setter
public abstract class AbstractEventView<T extends AbstractEvent> implements Message, Serializable, XmlStreamedContentProducer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final DateTimeFormatter FORMATTER_FOR_FILE_NAME = DateTimeFormatter.ofPattern("ddMMyyyy_HHmm");

    @Autowired
    private EventStatusDictionary eventStatusDictionary;

    @Autowired
    private EventCodeDictionary eventCodeDictionary;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserSession userSession;

    private AbstractEventModel<T> model;

    private EventFilter filter;

    private List<Department> departments;

    public abstract String getTitleXmlFile();

    public abstract String getPrefixNameFileXml();

    public abstract String getHeaderView();

    @Transactional
    public void init(AbstractEventModel abstractEventModel) {
        model = abstractEventModel;
        model.setFilter(filter.clone());
        departments = departmentService.getDepartmentFlatTree();
    }

    public void updateFilter() {
        model.setSelected(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:events")).reset();
    }

    @Override
    public String generateXml() {
        StringBuilder xmlText = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xmlText.append("<Events_List>");
        xmlText.append("<Title>").append(getTitleXmlFile()).append("</Title>");
        xmlText.append(generateXmlFilter());
        xmlText.append("<Events>");
        if (!model.getEvents().isEmpty()) {
            model.getEvents().forEach(event -> xmlText.append(generateXmlModel(event)));
        }
        return xmlText.append("</Events>").append("</Events_List>").toString();
    }

    private String generateXmlModel(T event) {
        StringBuilder xmlText = new StringBuilder("<Event>");
        xmlText.append("<Event_Date>").append(Objects.nonNull(event.getDate()) ?
                                                event.getDate().atZone(userSession.getUser().getDepartment().getZoneId())
                                                     .format(DATE_TIME_FORMATTER) : "").append("</Event_Date>");
        xmlText.append("<Result>").append(eventStatusDictionary.findOne(event.getStatus()).getValue()).append("</Result>");
        xmlText.append("<Code>").append(eventCodeDictionary.findOne(event.getCode()).getValue()).append("</Code>");
        xmlText.append("<Description>").append(event.getDescription()).append("</Description>");
        xmlText.append("<Login>").append(event.getUser().getLogin()).append("</Login>");
        xmlText.append("<Lastname>").append(event.getUser().getLastname()).append("</Lastname>");
        xmlText.append("<Firstname>").append(event.getUser().getFirstname()).append("</Firstname>");
        xmlText.append("<Patronymic>").append(event.getUser().getPatronymic()).append("</Patronymic>");
        xmlText.append("<Department>").append(event.getDepartment().getName()).append("</Department>");
        xmlText.append("<Project>").append(event.getProject().getName()).append("</Project>");
        xmlText.append("<Task>").append(event.getTask().getName()).append("</Task>");
        return xmlText.append("</Event>").toString();
    }

    private String generateXmlFilter() {
        StringBuilder xmlText = new StringBuilder("<Filter_Parameters>");
        if (!filter.getTypes().isEmpty()) {
            xmlText.append("<Results>");
            filter.getTypes()
                  .forEach(type -> xmlText.append("<Result>").append(eventStatusDictionary.findOne(type).getValue()).append("</Result>"));
            xmlText.append("</Results>");
        }
        if (!filter.getCodes().isEmpty()) {
            xmlText.append("<Codes>");
            filter.getCodes().forEach(code -> xmlText.append("<Code>").append(eventCodeDictionary.findOne(code).getValue()).append("</Code>"));
            xmlText.append("</Codes>");
        }
        if (Objects.nonNull(filter.getEventDateWith())) {
            xmlText.append("<Event_Date_From>")
                   .append(filter.getEventDateWith().atZone(userSession.getUser().getDepartment().getZoneId()).format(DATE_TIME_FORMATTER))
                   .append("</Event_Date_From>");
        }
        if (Objects.nonNull(filter.getEventDate())) {
            xmlText.append("<Event_Date_To>")
                   .append(filter.getEventDate().atZone(userSession.getUser().getDepartment().getZoneId()).format(DATE_TIME_FORMATTER))
                   .append("</Event_Date_To>");
        }
        if (Objects.nonNull(filter.getEventId())) {
            xmlText.append("<Connection_ID>").append(filter.getEventId()).append("</Connection_ID>");
        }
        if (!filter.getUsers().isEmpty()) {
            xmlText.append("<Users>");
            filter.getUsers().forEach(user -> xmlText.append("<User>").append(user.getLogin()).append("</User>"));
            xmlText.append("</Users>");
        }
        if (!filter.getDepartments().isEmpty()) {
            xmlText.append("<Departments>");
            filter.getDepartments().forEach(department -> xmlText.append("<Department>").append(department.getName()).append("</Department>"));
            xmlText.append("</Departments>");
        }
        if (!filter.getProjects().isEmpty()) {
            xmlText.append("<Projects>");
            filter.getProjects().forEach(project -> xmlText.append("<Project>").append(project.getName()).append("</Project>"));
            xmlText.append("</Projects>");
        }
        if (!filter.getTasks().isEmpty()) {
            xmlText.append("<Tasks>");
            filter.getTasks().forEach(task -> xmlText.append("<Task>").append(task.getName()).append("</Task>"));
            xmlText.append("</Tasks>");
        }
        return xmlText.append("</Filter_Parameters>").toString();
    }

    public StreamedContent fileDownload() {
        model.loadAllEvents();
        return fileDownload(
                LocalDateTime.now(userSession.getUser().getDepartment().getZoneId()).format(FORMATTER_FOR_FILE_NAME) + getPrefixNameFileXml());
    }
}
