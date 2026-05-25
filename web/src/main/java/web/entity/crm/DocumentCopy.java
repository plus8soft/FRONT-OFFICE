/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
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
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import web.entity.core.User;
import web.entity.dict.DocumentType;

@Getter
@Setter
@Log4j2
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "DOC_COPIES", schema = "CRM")
public class DocumentCopy implements Serializable {

    @Id
    @SequenceGenerator(name = "DOC_COPIES_ID_SEQ", sequenceName = "DOC_COPIES_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOC_COPIES_ID_SEQ")
    @Column(name = "DOC_COPIES_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONS_ID")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID")
    private User user;

    @Column(name = "CDATE")
    private Instant creationDate;

    @Column(name = "TYPE")
    private boolean external;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOC_TYPES_BANK_ID")
    private DocumentType bankDocumentType;

    @Column(name = "DOC_TYPES_CLIENT_ID")
    private String clientDocumentType;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SERIES")
    private String series;

    @Column(name = "NUMBER")
    private String number;

    @Column(name = "DOC_DATE")
    private LocalDate issuanceDate;

    @Column(name = "VALID_TO_DATE")
    private LocalDate validUntilDate;

    @Column(name = "TERMLESS")
    private Boolean termless;

    @Column(name = "VERSION")
    private Integer version;

    @Column(name = "MAIN")
    private Boolean main;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Transient
    private byte[] file;
}
