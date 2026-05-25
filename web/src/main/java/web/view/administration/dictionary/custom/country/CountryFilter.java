/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.country;

import java.io.Serializable;
import lombok.Data;

@Data
public class CountryFilter implements Serializable, Cloneable {

    private String iso;

    private String alpha2;

    private String alpha3;

    private Boolean status;

    private String name;

    private boolean extendedSearch;

    @Override
    public CountryFilter clone() {
        try {
            return (CountryFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
