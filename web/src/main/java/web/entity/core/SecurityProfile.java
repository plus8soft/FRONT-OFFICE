/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "SECURITY_PROFILES", schema = "CORE")
public class SecurityProfile implements Serializable {

    @Id
    @SequenceGenerator(name = "SECURITY_PROFILES_ID_SEQ", sequenceName = "SECURITY_PROFILES_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SECURITY_PROFILES_ID_SEQ")
    @Column(name = "SECURITY_PROFILES_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Embedded
    private PasswordComplexity passwordComplexity = new PasswordComplexity();

    @Enumerated(EnumType.STRING)
    @Column(name = "PASS_EXP_TERM_TYPE")
    private ChronoUnit passwordExpirationTermUnit;

    @Column(name = "PASS_EXP_TERM")
    private Integer passwordExpirationTerm;

    @Embedded
    private Lock lock = new Lock();

    @Column(name = "MAX_SESSIONS")
    private Integer maxSessions;

    @Column(name = "SESSION_TIMEOUT")
    private Integer sessionTimeout;

    @Column(name = "CONNECT_ONLY_FROM_TRUST_ZONE")
    private boolean connectOnlyFromTrustZone;

    @Column(name = "CONNECT_TRUST_ZONE")
    private String trustZone;

    @OneToMany(mappedBy = "securityProfile")
    private List<User> users = new ArrayList<>();
}
