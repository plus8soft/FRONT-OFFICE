/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.core;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PasswordComplexity implements Serializable {

    @Column(name = "PASS_UCASE_AND_LCASE")
    private boolean requireMixedRegister;

    @Column(name = "PASS_CONTAINS_NUMBERS")
    private boolean requireNumbers;

    @Column(name = "PASS_CONTAINS_SPEC_CHARACTERS")
    private boolean requireSpecialCharacters;

    @Column(name = "PASS_NOT_USE_USERDATA")
    private boolean forbidUserData;

    @Column(name = "PASS_NOT_USE_COMMON_WORDS")
    private boolean forbidCommonWords;

    @Column(name = "PASS_MIN_LEN")
    private Integer minLength;
}
