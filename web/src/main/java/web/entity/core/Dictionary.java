/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.time.Instant;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
@Table(name = "DICTIONARIES", schema = "CORE")
public class Dictionary {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "DICT_NAME")
    private DictionaryName id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "GROUP_NAME")
    private String group;

    @Column(name = "UPD_DATE")
    private Instant updateDate;

    @Column(name = "IS_SYSTEM")
    private boolean system;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(mappedBy = "dictionary", cascade = CascadeType.REMOVE)
    private List<DictionaryValue> dictionaryValues;
}
