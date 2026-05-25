/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@MappedSuperclass
public class BasePerson implements Serializable {

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
    private String birthCountry;

    @Column(name = "PLACEOFBTXT")
    private String birthPlace;

    @Column(name = "BIRTHDATE")
    private LocalDate birthDate;
}
