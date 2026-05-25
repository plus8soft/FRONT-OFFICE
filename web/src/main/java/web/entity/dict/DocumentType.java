/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.crm.DocumentCopy;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "DOC_TYPES", schema = "DICT")
public class DocumentType implements Serializable {

    @Id
    @SequenceGenerator(name = "DOC_TYPES_ID_SEQ", sequenceName = "DOC_TYPES_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOC_TYPES_ID_SEQ")
    @Column(name = "DOC_TYPES_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "TERM_TYPE")
    private ChronoUnit termType;

    @Column(name = "TERM")
    private Integer term;

    @Column(name = "TERMLESS")
    private Boolean termless;

    @Column(name = "DISABLED")
    private Boolean disabled;

    @Column(name = "REQUIRED")
    private Boolean required;

    @OneToMany(mappedBy = "bankDocumentType", cascade = CascadeType.REMOVE)
    private List<DocumentCopy> documentCopies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_TEMPLATES_ID")
    private ReportTemplate reportTemplate;
}
