/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.event.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import web.entity.log.UserEvent;
import web.view.administration.auditlogs.event.AbstractEventShowView;

@Getter
@Setter
@Log4j2
@NoArgsConstructor
public class UserEventShowView extends AbstractEventShowView<UserEvent> {

    @Override
    public String getTitleXmlFile() {
        return "Detailed_information_outuser_events";
    }

    @Override
    public String getPrefixNameFileXml() {
        return "_details_user_event.xml";
    }

    @Override
    public String getHeaderView() {
        return "Custom Events";
    }
}
