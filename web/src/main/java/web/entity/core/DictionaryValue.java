/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

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
@Table(name = "DICTIONARY_VALUES", schema = "CORE")
public class DictionaryValue {

    @Id
    @SequenceGenerator(name = "DICTIONARY_VALUES_ID_SEQ", sequenceName = "DICTIONARY_VALUES_ID_SEQ", schema = "CORE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DICTIONARY_VALUES_ID_SEQ")
    @Column(name = "DICTIONARY_VALUES_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DICT_NAME")
    private Dictionary dictionary;

    @Column(name = "CODE")
    private String code;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "SHORT_VALUE")
    private String shortValue;

    @Column(name = "IS_SYSTEM")
    private boolean system;

    @Column(name = "NOT_USE")
    private boolean unused;
}
