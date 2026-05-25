/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.administration.group;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import web.entity.core.Group;

@Service
public class GroupService {

    public String formatGroupPath(Group group, List<Group> all) {
        List<Group> parentGroups = new ArrayList<>();
        buildParentList(group.getParent(), parentGroups, all);
        parentGroups.add(group);
        return parentGroups.stream().map(Group::getName).collect(Collectors.joining(" > "));
    }

    private void buildParentList(Group child, List<Group> parentGroupBranches, List<Group> all) {
        all.stream().filter(group -> group.equals(child)).findFirst().ifPresent(group -> {
            buildParentList(group.getParent(), parentGroupBranches, all);
            parentGroupBranches.add(group);
        });
    }
}
