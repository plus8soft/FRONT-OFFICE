/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ps;

import java.time.LocalDate;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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
import web.entity.core.BaseAddress;
import web.entity.core.User;
import web.entity.crm.Gender;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "RECIPIENTS", schema = "PS")
@AttributeOverrides({@AttributeOverride(name = "code", column = @Column(name = "ADR_ADDRESS_CODE")),
                     @AttributeOverride(name = "postalCode", column = @Column(name = "ADR_POSTALCODE")),
                     @AttributeOverride(name = "country", column = @Column(name = "ADR_COUNTRY")),
                     @AttributeOverride(name = "regionType", column = @Column(name = "ADR_REGIONTYPE")),
                     @AttributeOverride(name = "region", column = @Column(name = "ADR_REGION")),
                     @AttributeOverride(name = "districtType", column = @Column(name = "ADR_DISTRICTTYPE")),
                     @AttributeOverride(name = "district", column = @Column(name = "ADR_DISTRICT")),
                     @AttributeOverride(name = "cityType", column = @Column(name = "ADR_CITYTYPE")),
                     @AttributeOverride(name = "city", column = @Column(name = "ADR_CITY")),
                     @AttributeOverride(name = "localityType", column = @Column(name = "ADR_LOCALITYTYPE")),
                     @AttributeOverride(name = "locality", column = @Column(name = "ADR_LOCALITY")),
                     @AttributeOverride(name = "streetType", column = @Column(name = "ADR_STREETTYPE")),
                     @AttributeOverride(name = "street", column = @Column(name = "ADR_STREET")),
                     @AttributeOverride(name = "house", column = @Column(name = "ADR_HOUSE")),
                     @AttributeOverride(name = "housing", column = @Column(name = "ADR_HOUSING")),
                     @AttributeOverride(name = "structure", column = @Column(name = "ADR_STRUCTURE")),
                     @AttributeOverride(name = "flat", column = @Column(name = "ADR_FLAT"))})
public class Recipient extends BaseAddress {

    @Id
    @SequenceGenerator(name = "RECIPIENTS_ID_SEQ", sequenceName = "RECIPIENTS_ID_SEQ", schema = "PS", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RECIPIENTS_ID_SEQ")
    @Column(name = "RECIPIENTS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID")
    private User user;

    @Column(name = "RESCOUNTRY")
    private String residentCountry;

    @Enumerated
    @Column(name = "GENDER")
    private Gender gender;

    @Column(name = "CITIZENSHIP")
    private String citizenship;

    @Column(name = "LASTNAME")
    private String lastname;

    @Column(name = "FIRSTNAME")
    private String firstname;

    @Column(name = "PATRONYMIC")
    private String patronymic;


    @Column(name = "COUNTRYOFB")
    private Long birthCountry;

    @Column(name = "PLACEOFBTXT")
    private String birthPlace;

    @Column(name = "DATEOFBIRTH")
    private LocalDate birthDate;

    @Column(name = "DOC_TYPE")
    private String docType;

    @Column(name = "DOC_SERIES")
    private String docSeries;

    @Column(name = "DOC_NUMBER")
    private String docNumber;

    @Column(name = "DOC_ISSUANCEUNIT")
    private String docIssuanceUnit;

    @Column(name = "DOC_ISSUANCEDATE")
    private LocalDate docIssuanceDate;

    @Column(name = "DOC_ISSUANCEUNITID")
    private String docIssuanceUnitCode;

    @Column(name = "DOC_VALIDTODATE")
    private LocalDate docValidUntilDate;


    @Column(name = "CONTACT_MOB_TEL")
    private String mobilePhone;

    @Column(name = "CONTACT_HOUSE_TEL")
    private String homePhone;

    @Column(name = "CONTACT_EMAIL")
    private String email;
}
