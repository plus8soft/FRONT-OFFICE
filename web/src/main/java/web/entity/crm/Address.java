/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.core.BaseAddress;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "ADDRESS", schema = "CRM")
public class Address extends BaseAddress {

    @Id
    @SequenceGenerator(name = "ADDRESS_ID_SEQ", sequenceName = "ADDRESS_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADDRESS_ID_SEQ")
    @Column(name = "ADDRESS_ID")
    private Long id;

    @OneToMany(mappedBy = "address")
    private List<PersonAddress> personAddresses = new ArrayList<>();

    public Address(BaseAddress abstractAddress) {
        super(abstractAddress);
    }
}
