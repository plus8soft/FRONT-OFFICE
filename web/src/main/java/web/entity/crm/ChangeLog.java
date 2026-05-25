/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "CHANGES_LOG", schema = "CRM")
public class ChangeLog implements Serializable {

    @Id
    @SequenceGenerator(name = "CHANGES_LOG_ID_SEQ", sequenceName = "CHANGES_LOG_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHANGES_LOG_ID_SEQ")
    @Column(name = "CHANGES_LOG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANGES_ID")
    private Change change;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private ChangeType type;

    @Column(name = "ENTITY_NAME")
    private String entityName;

    @Column(name = "ENTITY_DESCRIPTION")
    private String entityDescription;

    @Column(name = "ENTITY_PK_VALUE")
    private Long entityPrimaryKey;

    @Column(name = "ENTITY_TYPE")
    private String entityType;

    @OneToMany(mappedBy = "changeLog")
    private List<ChangeLogData> changeLogDataList = new ArrayList<>();
}
