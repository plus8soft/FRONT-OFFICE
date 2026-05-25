/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "CHANGES_LOG_DATA", schema = "CRM")
public class ChangeLogData implements Serializable {

    @Id
    @SequenceGenerator(name = "CHANGES_LOG_DATA_ID_SEQ", sequenceName = "CHANGES_LOG_DATA_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHANGES_LOG_DATA_ID_SEQ")
    @Column(name = "CHANGES_LOG_DATA_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANGES_LOG_ID")
    private ChangeLog changeLog;

    @Column(name = "FIELD_NAME")
    private String fieldName;

    @Column(name = "FIELD_DESCRIPTION")
    private String fieldDescription;

    @Column(name = "OLD_VALUE")
    private String oldValue;

    @Column(name = "NEW_VALUE")
    private String newValue;
}
