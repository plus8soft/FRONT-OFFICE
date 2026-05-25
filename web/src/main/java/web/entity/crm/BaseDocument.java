/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
@MappedSuperclass
public class BaseDocument implements Serializable {

    @Column(name = "TYPE")
    private String type;

    @Column(name = "SERIES")
    private String series;

    @Column(name = "NUMBER")
    private String number;

    @Column(name = "ISSUANCEUNIT")
    private String issuanceUnit;

    @Column(name = "ISSUANCEUNITID")
    private String issuanceUnitCode;

    @Column(name = "ISSUANCEDATE")
    private LocalDate issuanceDate;

    @Column(name = "VALIDTODATE")
    private LocalDate validUntilDate;

    public BaseDocument(BaseDocument document) {
        type = document.type;
        series = document.series;
        number = document.number;
        issuanceUnit = document.issuanceUnit;
        issuanceUnitCode = document.issuanceUnitCode;
        issuanceDate = document.issuanceDate;
        validUntilDate = document.validUntilDate;
    }
}
