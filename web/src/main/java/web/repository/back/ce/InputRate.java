/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.repository.back.ce;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import web.repository.back.meta.Column;
import web.repository.back.meta.Table;

@Table(name = "fr_pInsertCourseCurrency_in", index = "XPKfr_pInsertCourseCurrency_in")
@AllArgsConstructor
@Data
public class InputRate {

    @Column(name = "ISONumber")
    private String iso;

    @Column(name = "Value")
    private Integer ratio;

    @Column(name = "Course")
    private BigDecimal externalRate;

    @Column(name = "CourseBid")
    private BigDecimal buyRate;

    @Column(name = "CourseOffer")
    private BigDecimal sellRate;

    @Column(name = "Date")
    private LocalDateTime date;

    @Column(name = "TypeCourse")
    private String type;

    @Column(name = "OrderNumber")
    private Long orderNumber;

    @Column(name = "OrderDate")
    private LocalDateTime orderDate;

    @Column(name = "OrderName")
    private String orderName;
}
