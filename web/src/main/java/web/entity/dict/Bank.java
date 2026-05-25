/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(schema = "DICT", name = "BANKS")
public class Bank implements Serializable {

    @Id
    @SequenceGenerator(name = "BANK_ID_SEQ", sequenceName = "BANK_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BANK_ID_SEQ")
    @Column(name = "BANK_ID")
    private Long id;

    @Column(name = "FULL_NAME")
    private String name;

    @Column(name = "SHORT_NAME")
    private String shortName;

    @Column(name = "CORR_ACCOUNT")
    private String correspondentAccount;

    @Column(name = "ROUTING_NUMBER")
    private String routingNumber;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "[CREATE_DATE]")
    private LocalDate createDate;

    @Column(name = "[UPDATE_DATE]")
    private LocalDate updateDate;

    @Column(name = "[ACTUAL_DATE]")
    private LocalDate actualDate;
}
