/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.Department;
import web.entity.core.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "ORDERS", schema = "CE")
public class Order implements Serializable {

    @Id
    @SequenceGenerator(name = "ORDERS_ID_SEQ", sequenceName = "ORDERS_ID_SEQ", schema = "CE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORDERS_ID_SEQ")
    @Column(name = "ORDERS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERS_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEAL_USERS_ID")
    private User dealUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANCELED_ORDERS_ID")
    private Order canceled;

    @Column(name = "LDATE")
    private LocalDateTime date;

    @Column(name = "NUM")
    private Long number;

    @Enumerated
    @Column(name = "DEAL_STATUS")
    private DealStatus dealStatus;

    @Column(name = "DEAL_STATUS_DATE")
    private LocalDateTime dealStatusDate;

    @Column(name = "ALLOWED_OPERATIONS")
    private Integer allowedOperationsCount;

    @Column(name = "PERFORMED_OPERATIONS")
    private Integer performedOperationsCount;

    @OneToMany(mappedBy = "order")
    private List<CurrencyOperation> operations = new ArrayList<>();
}
