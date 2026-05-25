/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.account;

import java.time.LocalDateTime;
import lombok.Data;
import web.repository.back.converter.BigDecimalToLongConverter;
import web.repository.back.converter.StringConverter;
import web.repository.back.converter.TimestampToLocalDateTimeConverter;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pSysAccountList_out", index = "XPKfr_pSysAccountList_out")
@Data
public class OutputAccount {

    @Column(name = "NumberAccount", converter = StringConverter.class)
    private String id;

    @Column(name = "SubDivisionID", converter = BigDecimalToLongConverter.class)
    private Long departmentExternalId;

    @Column(name = "SubDivisionName", converter = StringConverter.class)
    private String departmentName;

    @Column(name = "BranchID", converter = BigDecimalToLongConverter.class)
    private Long parentDepartmentExternalId;

    @Column(name = "BranchName", converter = StringConverter.class)
    private String parentDepartmentName;

    @Column(name = "CurrencyISONumber", converter = StringConverter.class)
    private String currencyId;

    @Column(name = "CurrencyBrief", converter = StringConverter.class)
    private String currencyIso;

    @Column(name = "AccProfile", converter = StringConverter.class)
    private String profile;

    @Column(name = "Name", converter = StringConverter.class)
    private String name;

    @Column(name = "DateStart", converter = TimestampToLocalDateTimeConverter.class)
    private LocalDateTime openDate;

    @Column(name = "DateEnd", converter = TimestampToLocalDateTimeConverter.class)
    private LocalDateTime closeDate;
}
