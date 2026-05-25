/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.log;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "SYSTEM_EVENTS", schema = "LOG")
public class SystemEvent extends AbstractEvent {

    @Id
    @SequenceGenerator(name = "SYSTEM_EVENTS_ID_SEQ", sequenceName = "SYSTEM_EVENTS_ID_SEQ", schema = "LOG", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SYSTEM_EVENTS_ID_SEQ")
    @Column(name = "SYSTEM_EVENTS_ID")
    private Long id;
}
