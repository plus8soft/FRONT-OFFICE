/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.dict;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = {"level", "type"})
@Entity
@IdClass(AbbreviationId.class)
/**
 * Address object type abbreviations.
 * Maps to ADDRESS_ABBREVIATIONS table.
 */
@Table(name = "ADDRESS_ABBREVIATIONS", schema = "DICT")
public class Abbreviation implements Serializable {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "[LEVEL]")
    private Level level;

    @Id
    @Column(name = "SCNAME")
    private String type;

    @Column(name = "SOCRNAME")
    private String name;
}
