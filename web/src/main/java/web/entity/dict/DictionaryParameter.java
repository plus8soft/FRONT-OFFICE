/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(schema = "DICT", name = "DICTIONARY_PARAMS")
public class DictionaryParameter implements Serializable {

    @Id
    @SequenceGenerator(name = "DICTIONARY_PARAMS_ID_SEQ", sequenceName = "DICTIONARY_PARAMS_ID_SEQ", schema = "DICT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DICTIONARY_PARAMS_ID_SEQ")
    @Column(name = "DICTIONARY_PARAMS_ID")
    private Long id;

    @Column(name = "GROUP_NAME")
    private String group;

    @Enumerated(EnumType.STRING)
    @Column(name = "SYSTEM_NAME")
    private DictionarySystemName system;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ENABLE")
    private Boolean enabled;

    @Column(name = "UPDATE_TYPE")
    @Enumerated(EnumType.STRING)
    private UpdateType updateType;

    @Column(name = "UPDATE_FREQUENCY")
    private String schedule;

    @Column(name = "LAST_UPDATE_DATE")
    private Instant successUpdateDate;

    @Column(name = "SOURCE_UPDATE_DATE")
    private LocalDateTime sourceUpdateDate;

    @Column(name = "VERSION")
    private Long version;

    @Enumerated
    @Column(name = "LAST_UPDATE_STATUS")
    private UpdateResult lastUpdateResult;

    @Column(name = "LAST_UPDATE_RESULT")
    private String errorMessage;
}
