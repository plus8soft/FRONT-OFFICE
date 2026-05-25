/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;

public interface CheckboxTree extends Tree {

    default <T, U extends Comparable<? super U>> void buildTreeWithEmptyMessage(TreeNode parent, Function<T, T> parentFunction,
                                                                                Function<T, U> sortFunction, List<T> all, Set<T> selected,
                                                                                boolean selectable, boolean propagate) {
        if (all.isEmpty()) {
            addEmptyMessage(parent, "No data");
        } else {
            buildTree(parent, parentFunction, sortFunction, all, selectable);
            if (selected != null && !selected.isEmpty()) {
                streamTree(parent).filter(treeNode -> treeNode.getParent() != null && selected.contains(treeNode.getData()))
                                  .forEach(treeNode -> ((CheckboxTreeNode) treeNode).setSelected(true, propagate));
            }
        }
    }

    default <T, U extends Comparable<? super U>> void buildTree(TreeNode parent, Function<T, T> parentFunction, Function<T, U> sortFunction,
                                                                List<T> all, boolean selectable) {
        Stream<T> stream = parentFunction == null ? all.stream() : all.stream().filter(t -> !CheckboxTreeNode.DEFAULT_TYPE.equals(parent.getType()) ?
                                                                                            parentFunction.apply(t) == null :
                                                                                            Objects.equals(parentFunction.apply(t),
                                                                                                           parent.getData()));
        if (sortFunction != null) {
            stream = stream.sorted(Comparator.comparing(sortFunction));
        }
        stream.forEach(t -> {
            CheckboxTreeNode treeNode = new CheckboxTreeNode(t, parent);
            treeNode.setExpanded(true);
            treeNode.setSelectable(selectable);
            if (parentFunction != null) {
                buildTree(treeNode, parentFunction, sortFunction, all, selectable);
            }
        });
    }

    default <T, K> void buildGroupingTree(TreeNode parent, Function<T, K> groupingFunction, Function<T, T> parentFunction, List<T> all,
                                          Set<T> selected, boolean selectable, boolean propagate) {
        if (all.isEmpty()) {
            addEmptyMessage(parent, "No data");
        } else {
            final String type = "group";
            all.stream().collect(Collectors.groupingBy(groupingFunction)).entrySet().forEach(entry -> {
                CheckboxTreeNode node = new CheckboxTreeNode(type, entry.getKey(), parent);
                node.setExpanded(true);
                node.setSelectable(selectable);
                buildTree(node, parentFunction, null, entry.getValue(), selectable);
            });
            if (selected != null && !selected.isEmpty()) {
                streamTree(parent)
                        .filter(treeNode -> treeNode.getParent() != null && !type.equals(treeNode.getType()) && selected.contains(treeNode.getData()))
                        .forEach(treeNode -> ((CheckboxTreeNode) treeNode).setSelected(true, propagate));
            }
        }
    }

    default <T, K> void buildGroupingTree(TreeNode parent, Function<T, K> groupingFunction, List<T> all, Set<T> selected, boolean selectable) {
        buildGroupingTree(parent, groupingFunction, null, all, selected, selectable, true);
    }
}
