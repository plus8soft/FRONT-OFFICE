/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Lock implements Serializable {

    @Column(name = "BLOCK_ACCOUNT_INACTIVE")
    private boolean inactiveAccount;

    @Column(name = "BLOCK_ACCOUNT_INACTIVE_TERM")
    private Integer inactiveTerm;

    @Enumerated(EnumType.STRING)
    @Column(name = "BLOCK_ACCOUNT_INACTIVE_TERM_TYPE")
    private ChronoUnit inactiveTermUnit;

    @Column(name = "BLOCK_ACCOUNT_FAIL_LOGIN")
    private boolean exceedLoginFailureCount;

    @Column(name = "BLOCK_ACCOUNT_FAIL_LOGIN_ATTEMPS")
    private Integer loginFailureAttempts;

    @Column(name = "TEMPORARY_BLOCK_ACCOUNT")
    private boolean tempExceedLoginFailureCount;

    @Column(name = "TEMPORARY_BLOCK_ACCOUNT_FAIL_ATTEMPS")
    private Integer tempLoginFailureAttempts;

    @Enumerated(EnumType.STRING)
    @Column(name = "TEMPORARY_BLOCK_ACCOUNT_TERM_TYPE")
    private ChronoUnit tempLoginFailureTermUnit;

    @Column(name = "TEMPORARY_BLOCK_ACCOUNT_TERM")
    private Integer tempLoginFailureTerm;
}
