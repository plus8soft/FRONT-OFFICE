/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.log;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import web.entity.crm.BaseDocument;
import web.entity.crm.BasePerson;
import web.entity.crm.Person;

@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "OPERATION_PERSONS", schema = "LOG")
public class PersonHistory extends BasePerson {

    @Id
    @SequenceGenerator(name = "OPERATION_PERSONS_ID_SEQ", sequenceName = "OPERATION_PERSONS_ID_SEQ", schema = "LOG", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OPERATION_PERSONS_ID_SEQ")
    @Column(name = "OPERATION_PERSONS_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPERATIONS_ID")
    private Operation operation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONS_ID")
    private Person person;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "type", column = @Column(name = "DOC_TYPE")),
                         @AttributeOverride(name = "series", column = @Column(name = "DOC_SERIES")),
                         @AttributeOverride(name = "number", column = @Column(name = "DOC_NUMBER")),
                         @AttributeOverride(name = "issuanceUnit", column = @Column(name = "DOC_ISSUANCEUNIT")),
                         @AttributeOverride(name = "issuanceUnitCode", column = @Column(name = "DOC_ISSUANCEUNITID")),
                         @AttributeOverride(name = "issuanceDate", column = @Column(name = "DOC_ISSUANCEDATE")),
                         @AttributeOverride(name = "validUntilDate", column = @Column(name = "DOC_VALIDTODATE"))})
    private BaseDocument document = new BaseDocument();

    @Embedded
    private AddressHistory address = new AddressHistory();

    @SuppressWarnings("checkstyle:MissingDeprecated")
    @Deprecated
    @Transient
    private AddressHistory stayingAddress = new AddressHistory();

    @SuppressWarnings("checkstyle:MissingDeprecated")
    @Deprecated
    @Transient
    private boolean matchedAddresses;

    @Column(name = "CONTACT_MOB_TEL")
    private String mobilePhone;

    @Column(name = "CONTACT_HOUSE_TEL")
    private String homePhone;

    @Column(name = "CONTACT_EMAIL")
    private String email;

    @PrePersist
    private void prePersist() {
        setResidentCountry(person.getResidentCountry());
        setGender(person.getGender());
        setCitizenship(person.getCitizenship());
        setLastname(person.getLastname());
        setFirstname(person.getFirstname());
        setPatronymic(person.getPatronymic());
        setBirthCountry(person.getBirthCountry());
        setBirthPlace(person.getBirthPlace());
        setBirthDate(person.getBirthDate());
    }
}
