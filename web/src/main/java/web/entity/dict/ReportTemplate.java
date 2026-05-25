/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "REPORT_TEMPLATES", schema = "DICT")
public class ReportTemplate implements Serializable {

    @Id
    @SequenceGenerator(name = "REPORT_TEMPLATES_ID_SEQ", sequenceName = "REPORT_TEMPLATES_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_TEMPLATES_ID_SEQ")
    @Column(name = "REPORT_TEMPLATES_ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "SYSTEM_NAME")
    private ReportType systemName;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "GROUP_NAME")
    private String group;

    @Column(name = "TEMPLATE")
    private byte[] file;

    @Column(name = "TEMPLATE_SIZE")
    private Long size;

    @Column(name = "TEMPLATE_DATE")
    private Instant date;

    @OneToMany(mappedBy = "reportTemplate")
    private List<DocumentType> documentTypes;

    @OneToMany(mappedBy = "reportTemplate", cascade = CascadeType.REMOVE)
    private List<ReportTemplateContext> reportTemplateContexts = new ArrayList<>();
}
