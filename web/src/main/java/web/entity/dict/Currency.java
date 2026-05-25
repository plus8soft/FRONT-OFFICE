/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.ce.Case;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "CURRENCIES", schema = "DICT")
public class Currency implements Serializable {

    @Id
    @Column(name = "CURRENCIES_KD")
    private String id;

    @Column(name = "ISO")
    private String iso;

    @Column(name = "NAME")
    private String name;

    @Column(name = "RATIO")
    private Integer ratio;

    @Column(name = "IMG")
    private byte[] image;

    @Column(name = "GENDER")
    private boolean male;

    @Column(name = "ENABLE")
    private boolean enabled;

    @Column(name = "POSITION")
    private Integer position;

    @Column(name = "ALTERNATIVE_KD")
    private String alternativeCode;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "nominative", column = @Column(name = "INTEGRAL_VAL1")),
                         @AttributeOverride(name = "genitive", column = @Column(name = "INTEGRAL_VAL2")),
                         @AttributeOverride(name = "plural", column = @Column(name = "INTEGRAL_VAL3"))})
    private Case integralCase = new Case();

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "nominative", column = @Column(name = "FRACTIONAL_VAL1")),
                         @AttributeOverride(name = "genitive", column = @Column(name = "FRACTIONAL_VAL2")),
                         @AttributeOverride(name = "plural", column = @Column(name = "FRACTIONAL_VAL3"))})
    private Case fractionCase = new Case();

    @ManyToMany
    @JoinTable(name = "PS_POINTS_CURRENCYS", schema = "DICT",
               joinColumns = @JoinColumn(name = "CURRENCIES_KD", referencedColumnName = "CURRENCIES_KD"),
               inverseJoinColumns = @JoinColumn(name = "POINTS_ID", referencedColumnName = "POINTS_ID"))
    private Set<PaymentPoint> paymentPoints = new HashSet<>();

    @OneToMany(mappedBy = "currency")
    private List<Account> accounts = new ArrayList<>();
}
