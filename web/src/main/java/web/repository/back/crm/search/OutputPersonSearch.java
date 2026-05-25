/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.crm.search;

import java.time.LocalDate;
import lombok.Data;
import web.entity.crm.Gender;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.converter.ShortToGenderConverter;
import web.repository.back.converter.StringConverter;
import web.repository.back.converter.TimestampToLocalDateConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pClientSearchF_Srch_out", index = "XPKfr_pClientSearchF_Srch_out")
@Data
public class OutputPersonSearch {

    @Column(name = "InstitutionID", converter = BigDecimalToLongConverter.class)
    private Long externalId;

    @Column(name = "SurName", converter = StringConverter.class)
    private String lastname;

    @Column(name = "Name", converter = StringConverter.class)
    private String firstname;

    @Column(name = "PatronymicName", converter = StringConverter.class)
    private String patronymic;

    @Column(name = "ResidenceCountryISO", converter = StringConverter.class)
    private String residentCountry;

    @Column(name = "BirthCountryISO", converter = StringConverter.class)
    private String birthCountry;

    @Column(name = "BirthDay", converter = TimestampToLocalDateConverter.class)
    private LocalDate birthDate;

    @Column(name = "BirthPlace", converter = StringConverter.class)
    private String birthPlace;

    @Column(name = "Sex", converter = ShortToGenderConverter.class)
    private Gender gender;

    @Column(name = "CountryISO", converter = StringConverter.class)
    private String citizenship;

    @Column(name = "EIN", converter = StringConverter.class)
    private String ein;

    @Column(name = "Business", converter = StringConverter.class)
    private String bussinessType;
}
