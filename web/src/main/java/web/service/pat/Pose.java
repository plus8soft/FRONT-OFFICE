/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.service.pat.Operation;

@Setter
@Getter
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pose {

    private String id;

    private String name;

    private String address;

    private Region region;

    private List<String> currencies;

    private Set<Operation> operations;
}
