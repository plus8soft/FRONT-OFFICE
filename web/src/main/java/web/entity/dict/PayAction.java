/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(schema = "DICT", name = "PAY_ACTIONS")
public class PayAction implements Serializable {

    @Id
    @SequenceGenerator(name = "PAY_ACTIONS_ID_SEQ", sequenceName = "PAY_ACTIONS_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAY_ACTIONS_ID_SEQ")
    @Column(name = "PAY_ACTIONS_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CASH_SYMBOL")
    private String cashSymbol;

    @Column(name = "DISABLED")
    private boolean disabled;

    @Enumerated
    @Column(name = "TYPE")
    private PayActionType type;
}
