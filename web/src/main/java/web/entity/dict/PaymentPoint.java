/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "PS_POINTS", schema = "DICT")
public class PaymentPoint implements Serializable {

    @Id
    @SequenceGenerator(name = "PS_POINTS_ID_SEQ", sequenceName = "PS_POINTS_ID_SEQ", schema = "DICT", allocationSize = 20)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PS_POINTS_ID_SEQ")
    @Column(name = "POINTS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SYSTEMS_NAME")
    private PaymentSystem paymentSystem;

    @Column(name = "CODE")
    private String code;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ADDRESS")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRIES_ISO")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGIONS_ID")
    private Region region;

    @ManyToMany
    @JoinTable(name = "PS_POINTS_CURRENCYS", schema = "DICT", joinColumns = @JoinColumn(name = "POINTS_ID", referencedColumnName = "POINTS_ID"),
               inverseJoinColumns = @JoinColumn(name = "CURRENCIES_KD", referencedColumnName = "CURRENCIES_KD"))
    private Set<Currency> currencies = new HashSet<>();
}
