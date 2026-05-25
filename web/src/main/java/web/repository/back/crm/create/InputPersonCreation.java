/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.create;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import web.entity.crm.Gender;
import web.repository.back.converter.ShortToGenderConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@AllArgsConstructor
@Table(name = "fr_pClientCreateF_Crt_in", index = "XPKfr_pClientCreateF_Crt_in")
@Data
public class InputPersonCreation {

    @Column(name = "Name")
    private String firstname;

    @Column(name = "SurName")
    private String lastname;

    @Column(name = "PatronymicName")
    private String patronymic;

    @Column(name = "Sex", converter = ShortToGenderConverter.class)
    private Gender gender;

    @Column(name = "BirthDay")
    private LocalDate birthDate;

    @Column(name = "BirthPlace")
    private String birthPlace;

    @Column(name = "BirthCountryISO")
    private String birthCountry;

    @Column(name = "CountryISO")
    private String citizenship;

    @Column(name = "EIN")
    private String ein;

    @Column(name = "Resident")
    private Boolean resident;

    @Column(name = "ResidenceCountryISO")
    private String residentCountry;
}
