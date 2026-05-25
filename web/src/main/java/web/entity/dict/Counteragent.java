/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(schema = "DICT", name = "COUNTERAGENTS")
public class Counteragent {

    @Id
    @SequenceGenerator(name = "COUNTERAGENTS_ID_SEQ", sequenceName = "COUNTERAGENTS_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COUNTERAGENTS_ID_SEQ")
    @Column(name = "COUNTERAGENTS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID")
    private User user;

    @Column(name = "CDATE")
    private LocalDateTime date;

    @Column(name = "VERSION")
    private Long version;

    @Column(name = "EIN")
    private String ein;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "BANK_ROUTING_NUMBER")
    private String routingNumber;

    @Column(name = "CONTRACT")
    private boolean contract;

    @Column(name = "DISABLED")
    private boolean disabled;

    @Column(name = "PURPOSE_TEMPLATE")
    private String purposeTemplate;

    @OneToMany(mappedBy = "counteragent")
    private List<CounteragentPayAction> payActions = new ArrayList<>();
}
