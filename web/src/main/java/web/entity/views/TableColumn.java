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
@Builder
@EqualsAndHashCode(of = {"schema", "table", "column"})
@Entity
@IdClass(TableColumnId.class)
@Table(schema = "CORE", name = "TABLE_FIELDS_V")
public class TableColumn implements Serializable {

    @Id
    @Column(name = "schemaName")
    private String schema;

    @Id
    @Column(name = "tableName")
    private String table;

    @Id
    @Column(name = "columnName")
    private String column;

    @Column(name = "columnDescr")
    private String description;
}
