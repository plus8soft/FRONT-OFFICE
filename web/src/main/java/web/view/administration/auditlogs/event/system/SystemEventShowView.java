/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event.system;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import web.entity.log.SystemEvent;
import web.view.administration.auditlogs.event.AbstractEventShowView;

@Getter
@Setter
@Log4j2
@NoArgsConstructor
public class SystemEventShowView extends AbstractEventShowView<SystemEvent> {

    @Override
    public String getTitleXmlFile() {
        return "Detailed_System_Event_Information";
    }

    @Override
    public String getPrefixNameFileXml() {
        return "_details_system_event.xml";
    }

    @Override
    public String getHeaderView() {
        return "System Events";
    }
}
