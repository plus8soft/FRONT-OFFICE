/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
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
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "PERSONS", schema = "CRM")
public class Person extends BasePerson implements Serializable {

    @Id
    @SequenceGenerator(name = "PERSONS_ID_SEQ", sequenceName = "PERSONS_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERSONS_ID_SEQ")
    @Column(name = "PERSONS_ID")
    private Long id;

    @Column(name = "EXT_PERSONS_ID")
    private Long externalId;

    @Column(name = "LASTNAME_RDP")
    private String lastnameGenitive;

    @Column(name = "FIRSTNAME_RDP")
    private String firstnameGenitive;

    @Column(name = "PATRONYMIC_RDP")
    private String patronymicGenitive;

    @Column(name = "LASTNAME_DTP")
    private String lastnameDative;

    @Column(name = "FIRSTNAME_DTP")
    private String firstnameDative;

    @Column(name = "PATRONYMIC_DTP")
    private String patronymicDative;

    @Column(name = "LASTNAME_TVP")
    private String lastnameInstrumental;

    @Column(name = "FIRSTNAME_TVP")
    private String firstnameInstrumental;

    @Column(name = "PATRONYMIC_TVP")
    private String patronymicInstrumental;

    @Column(name = "BUSINESS")
    private String businessType;


    @Column(name = "EIN")
    private String ein;

    @Column(name = "STATUS")
    private Integer status;

    @Column(name = "ATTRACTION_SOURCE")
    private String attractionSource;

    @OneToMany(mappedBy = "person")
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<PersonAddress> personAddresses = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<Contact> contacts = new ArrayList<>();

    @OneToOne(mappedBy = "person", fetch = FetchType.LAZY)
    private Photo photo;

    @OneToMany(mappedBy = "person")
    private List<DocumentCopy> documentCopies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<Change> changes = new ArrayList<>();

    @Transient
    private Boolean terrorist;

    @Transient
    private Boolean fmsInvalid;
}
