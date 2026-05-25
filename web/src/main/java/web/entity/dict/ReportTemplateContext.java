/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "REPORT_TEMPLATES_DATA", schema = "DICT")
public class ReportTemplateContext implements Serializable {

    @Id
    @SequenceGenerator(name = "REPORT_TEMPLATES_DATA_ID_SEQ", sequenceName = "REPORT_TEMPLATES_DATA_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_TEMPLATES_DATA_ID_SEQ")
    @Column(name = "REPORT_TEMPLATES_DATA_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_TEMPLATES_ID")
    private ReportTemplate reportTemplate;

    @Column(name = "SYSTEM_NAME")
    private ContextType type;
}
