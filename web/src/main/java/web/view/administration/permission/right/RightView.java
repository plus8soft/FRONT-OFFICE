/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.permission.right;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import web.repository.core.RoleRepository;

@Getter
@Setter
public class RightView implements Serializable {

    @Autowired
    private RoleRepository roleRepository;

    private RightModel rightModel;

    private RightItem rightItem;

    public void init(RightModel rightModel) {
        this.rightModel = rightModel;
    }
}
