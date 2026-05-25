/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.lob.entity.crm;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(schema = "CRM", name = "DOC_COPIES_DATA")
public class LobDocumentCopy implements Serializable {

    @Id
    @Column(name = "DOC_COPIES_ID")
    private Long id;

    @Column(name = "DATA")
    private byte[] data;
}
