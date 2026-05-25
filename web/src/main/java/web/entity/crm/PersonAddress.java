/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "PERSON_ADDRESS", schema = "CRM")
public class PersonAddress implements Serializable {

    @Id
    @SequenceGenerator(name = "PERSON_ADDRESS_ID_SEQ", sequenceName = "PERSON_ADDRESS_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERSON_ADDRESS_ID_SEQ")
    @Column(name = "PERSON_ADDRESS_ID")
    private Long id;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "TYPE_MATCH")
    private String matchType;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONS_ID")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_ID")
    private Address address;
}
