/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
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
import web.entity.ce.RateType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "EXT_CURRENCY_RATES", schema = "DICT")
public class ExtRate {

    @Id
    @SequenceGenerator(name = "EXT_CURRENCY_RATES_ID_SEQ", sequenceName = "EXT_CURRENCY_RATES_ID_SEQ", schema = "DICT", allocationSize = 20)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EXT_CURRENCY_RATES_ID_SEQ")
    @Column(name = "EXT_CURRENCY_RATES_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENCIES_KD")
    private Currency currency;

    @Column(name = "DATE")
    private LocalDateTime date;

    @Column(name = "RATIO")
    private Integer ratio;

    @Enumerated(EnumType.STRING)
    @Column(name = "SRC")
    private RateType type;

    @Column(name = "SELL_RATE")
    private BigDecimal sellRate;

    @Column(name = "BUY_RATE")
    private BigDecimal buyRate;
}
