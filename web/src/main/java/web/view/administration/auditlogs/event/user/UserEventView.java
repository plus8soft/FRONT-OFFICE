/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event.user;

import web.view.administration.auditlogs.event.AbstractEventView;

public class UserEventView extends AbstractEventView {

    @Override
    public String getTitleXmlFile() {
        return "User_Events_List";
    }

    @Override
    public String getPrefixNameFileXml() {
        return "_user_events.xml";
    }

    @Override
    public String getHeaderView() {
        return "Custom Events";
    }
}
