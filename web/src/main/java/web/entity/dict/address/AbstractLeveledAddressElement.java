/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict.address;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import web.entity.dict.Level;

@Getter
@Setter
@Entity
@Inheritance
@DiscriminatorColumn(name = "[LEVEL]", discriminatorType = DiscriminatorType.INTEGER)
/**
 * Address elements: regions, districts, localities.
 * Maps to ADDRESS_ELEMENTS table.
 */
@Table(name = "ADDRESS_ELEMENTS", schema = "DICT")
public abstract class AbstractLeveledAddressElement extends AbstractAddressElement {

    @Enumerated
    @Column(name = "[LEVEL]", insertable = false, updatable = false)
    private Level level;
}
