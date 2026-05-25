/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
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
import web.entity.core.Department;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "ACCOUNT_LINKS", schema = "DICT")
public class AccountLink implements Serializable {

    @Id
    @SequenceGenerator(name = "ACCOUNT_LINKS_ID_SEQ", sequenceName = "ACCOUNT_LINKS_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_LINKS_ID_SEQ")
    @Column(name = "ACCOUNT_LINKS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENTS_ID")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "OBJ_TYPE")
    private ObjectType objectType;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "NIGHTLY")
    private boolean nightly;

    @Column(name = "OPEN_DATE")
    private LocalDateTime openDate;

    @Column(name = "CLOSE_DATE")
    private LocalDateTime closeDate;
}
