/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.views;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
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
@EqualsAndHashCode(of = {"schema", "table"})
@Builder
@Entity
@IdClass(TableDescriptionId.class)
@Table(schema = "CORE", name = "TABLE_DESCRIPTIONS_V")
public class TableDescription implements Serializable {

    @Id
    @Column(name = "schemaName")
    private String schema;

    @Id
    @Column(name = "tableName")
    private String table;

    @Column(name = "tableDescr")
    private String description;
}
