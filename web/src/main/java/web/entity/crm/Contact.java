/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
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
@Table(name = "CONTACTS", schema = "CRM")
public class Contact implements Serializable {

    @Id
    @SequenceGenerator(name = "CONTACTS_ID_SEQ", sequenceName = "CONTACTS_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTACTS_ID_SEQ")
    @Column(name = "CONTACTS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONS_ID")
    private Person person;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "DATA")
    private String data;

    @Column(name = "NOTIFICATION")
    private boolean notification;

    @Column(name = "MAIN")
    private boolean main;

    @Column(name = "DESCRIPTION")
    private String description;
}
