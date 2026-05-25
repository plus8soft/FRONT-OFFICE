/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
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
@Table(schema = "DICT", name = "DBF_LOAD_RESULT")
public class DbfLoadResult implements Serializable {

    @Id
    @Column(name = "SPID")
    private Long id;

    @Column(name = "SVLNAME")
    private String dictionaryName;

    @Column(name = "LASTACTION")
    private String action;

    @Column(name = "LASTACTIONCMD")
    private String actionCommand;

    @Column(name = "LOADEDVERSION")
    private Long version;

    @Enumerated
    @Column(name = "ERRCODE")
    private UpdateResult errorCode;

    @Column(name = "ERRTEXT")
    private String errorText;
}
