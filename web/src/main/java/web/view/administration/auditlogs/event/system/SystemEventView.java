/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event.system;

import web.view.administration.auditlogs.event.AbstractEventView;

public class SystemEventView extends AbstractEventView {

    @Override
    public String getTitleXmlFile() {
        return "System_Events_List";
    }

    @Override
    public String getPrefixNameFileXml() {
        return "_system_events.xml";
    }

    @Override
    public String getHeaderView() {
        return "System Events";
    }
}
