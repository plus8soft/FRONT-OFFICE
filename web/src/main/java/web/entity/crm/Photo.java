/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.crm;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
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
@Table(name = "PERSON_PHOTO", schema = "CRM")
public class Photo implements Serializable {

    @Id
    @SequenceGenerator(name = "PERSON_PHOTO_ID_SEQ", sequenceName = "PERSON_PHOTO_ID_SEQ", schema = "CRM", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERSON_PHOTO_ID_SEQ")
    @Column(name = "PERSON_PHOTO_ID")
    private Long id;

    @Transient
    private byte[] image;

    @Column(name = "PHOTO_DATE")
    private Instant dateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private AdditionMethod additionMethod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONS_ID")
    private Person person;
}
