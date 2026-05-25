/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "AUDIT_PARAMS", schema = "CORE")
public class EventSetting implements Serializable {

    @Id
    @SequenceGenerator(name = "AUDIT_PARAMS_ID_SEQ", sequenceName = "AUDIT_PARAMS_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AUDIT_PARAMS_ID_SEQ")
    @Column(name = "AUDIT_PARAMS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASKS_ID")
    private Task task;

    @Enumerated
    @Column(name = "TYPE")
    private EventType type;

    @Column(name = "EVENT_CODE")
    private EventCode code;

    @Column(name = "FIX_EVENT")
    private boolean enabled;

    @Column(name = "IS_SYSTEM")
    private boolean editable;

    @Column(name = "DESCRIPTION")
    private String description;
}
