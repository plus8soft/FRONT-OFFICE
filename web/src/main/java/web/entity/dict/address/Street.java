/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict.address;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import web.entity.dict.Level;

@Getter
@Setter
@Entity
/**
 * City and locality streets.
 * Maps to ADDRESS_STREETS table.
 */
@Table(name = "ADDRESS_STREETS", schema = "DICT")
public class Street extends AbstractAddressElement implements LeveledAddressElement {

    @Override
    public Level getLevel() {
        return Level.STREET;
    }
}
