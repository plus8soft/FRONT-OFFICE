/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.user.group;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.core.Group;
import web.entity.core.Role_;
import web.entity.core.User_;
import web.repository.core.GroupRepository;
import web.repository.core.RoleRepository;
import web.repository.core.UserRepository;
import web.view.DefaultTree;

@Getter
@Setter
public class GroupView implements Serializable, DefaultTree {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private TreeNode tree;

    private TreeNode selectedTreeNode;

    public void init() {
        Function<GroupItem, Group> function = GroupItem::getGroup;
        buildTreeWithEmptyMessage(tree = new DefaultTreeNode(), function.andThen(Group::getParent), function, function.andThen(Group::getPosition),
                                  groupRepository.findByAreUserIs(true).stream().map(group -> new GroupItem(group, userRepository
                                          .count((root, query, cb) -> cb.isMember(group, root.get(User_.groups))), roleRepository
                                                                                                                    .count((root, query, cb) -> cb
                                                                                                                            .isMember(group, root.get(
                                                                                                                                    Role_.groups)))))
                                                 .collect(Collectors.toList()));
    }

    public Group add() {
        Group group = new Group();
        group.setParent(selectedTreeNode == null ? null : ((GroupItem) selectedTreeNode.getData()).getGroup());
        group.setPosition(selectedTreeNode == null ? tree.getChildCount() : selectedTreeNode.getChildCount());
        return group;
    }

    public void onDragDrop(TreeDragDropEvent event) {
        TreeNode parentNode = event.getDropNode();
        ((GroupItem) event.getDragNode().getData()).getGroup()
                                                   .setParent(parentNode.getData() == null ? null : ((GroupItem) parentNode.getData()).getGroup());
        IntStream.range(0, parentNode.getChildren().size()).forEachOrdered(index -> {
            Group child = ((GroupItem) parentNode.getChildren().get(index).getData()).getGroup();
            child.setPosition(index);
            groupRepository.save(child);
        });
    }

    @Transactional
    public void delete() {
        groupRepository.delete(((GroupItem) selectedTreeNode.getData()).getGroup());
        List<TreeNode> children =
                selectedTreeNode.getParent().getChildren().stream().filter(child -> !child.equals(selectedTreeNode)).collect(Collectors.toList());
        if (selectedTreeNode.getParent().getChildren().remove(selectedTreeNode)) {
            IntStream.range(0, children.size()).forEachOrdered(index -> {
                Group child = ((GroupItem) children.get(index).getData()).getGroup();
                child.setPosition(index);
                groupRepository.save(child);
            });
        }
        selectedTreeNode = null;
        if (tree.isLeaf()) {
            addEmptyMessage(tree, "No data");
        }
    }
}
