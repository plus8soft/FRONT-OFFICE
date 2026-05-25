/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.update;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import web.entity.crm.Gender;
import web.repository.back.converter.ShortToGenderConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pChangeClientF_in", index = "XPKfr_pChangeClientF_in")
@AllArgsConstructor
@Data
public class InputPersonChange {

    @Column(name = "SurName")
    private String lastname;

    @Column(name = "Name")
    private String firstname;

    @Column(name = "PatronymicName")
    private String patronymic;

    @Column(name = "ResidenceCountryISO")
    private String residentCountry;

    @Column(name = "Sex", converter = ShortToGenderConverter.class)
    private Gender gender;

    @Column(name = "BirthCountryISO")
    private String birthCountry;

    @Column(name = "BirthPlace")
    private String birthPlace;

    @Column(name = "BirthDay")
    private LocalDate birthDate;

    @Column(name = "EIN")
    private String ein;

    @Column(name = "CitizenCountryISO")
    private String citizenship;

    @Column(name = "ResidentFlag")
    private Boolean resident;
}
