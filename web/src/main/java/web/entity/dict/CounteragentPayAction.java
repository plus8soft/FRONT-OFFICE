/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import javax.persistence.Column;
import javax.persistence.Entity;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(schema = "DICT", name = "COUNTERAGENT_PAY_ACTIONS")
public class CounteragentPayAction {

    @Id
    @SequenceGenerator(name = "COUNTERAGENT_PAY_ACTIONS_ID_SEQ", sequenceName = "COUNTERAGENT_PAY_ACTIONS_ID_SEQ", schema = "DICT",
                       allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COUNTERAGENT_PAY_ACTIONS_ID_SEQ")
    @Column(name = "COUNTERAGENT_PAY_ACTIONS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTERAGENTS_ID")
    private Counteragent counteragent;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CASH_SYMBOL")
    private String cashSymbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT")
    private Account account;

    @Column(name = "VAT")
    private Integer vat;

    @Column(name = "MAIN")
    private Boolean main;

    @Enumerated
    @Column(name = "TYPE")
    private PayActionType type;
}
