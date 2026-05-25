/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.auditlogs.io;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import web.entity.AuthorizationResult;

@Data
public class ConnectionEventFilter implements Serializable, Cloneable {

    private String lastname;

    private String firstname;

    private String patronymic;

    private String userLogin;

    private String userIp;

    private Instant eventDateWith;

    private Instant eventDate;

    private Instant outDateWith;

    private Instant outDate;

    private Boolean result;

    private List<AuthorizationResult> authorizationResults = new ArrayList<>();

    @Override
    public ConnectionEventFilter clone() {
        try {
            return (ConnectionEventFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
