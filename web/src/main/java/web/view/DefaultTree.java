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
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public interface DefaultTree extends Tree {

    default <T, U extends Comparable<? super U>, R> void buildTreeWithEmptyMessage(TreeNode parent, Function<T, R> parentFunction,
                                                                                   Function<T, R> compareItemFunction, Function<T, U> sortFunction,
                                                                                   List<T> all) {
        if (all.isEmpty()) {
            addEmptyMessage(parent, "No data");
        } else {
            buildTree(parent, parentFunction, compareItemFunction, sortFunction, all);
        }
    }

    default <T, U extends Comparable<? super U>, R> void buildTreeWithEmptyMessage(TreeNode parent, Function<T, R> parentFunction,
                                                                                   Function<T, R> compareItemFunction, Function<T, U> sortFunction,
                                                                                   List<T> all, Set<R> selectable) {
        if (all.isEmpty()) {
            addEmptyMessage(parent, "No data");
        } else {
            buildTree(parent, parentFunction, compareItemFunction, sortFunction, all, selectable);
        }
    }

    default <T, U extends Comparable<? super U>, R> void buildTree(TreeNode parent, Function<T, R> parentFunction, Function<T, R> compareItemFunction,
                                                                   Function<T, U> sortFunction, List<T> all) {
        Stream<T> stream = all.stream().filter(t -> parent.getData() == null ? parentFunction.apply(t) == null :
                                                    Objects.equals(parentFunction.apply(t), compareItemFunction.apply((T) parent.getData())));
        if (sortFunction != null) {
            stream = stream.sorted(Comparator.comparing(sortFunction));
        }
        stream.forEach(t -> {
            DefaultTreeNode treeNode = new DefaultTreeNode(t, parent);
            treeNode.setExpanded(true);
            buildTree(treeNode, parentFunction, compareItemFunction, sortFunction, all);
        });
    }

    default <T, R> void buildTree(TreeNode parent, Function<T, R> parentFunction, Function<T, R> compareItemFunction, List<T> all,
                                  boolean nodeSelectable) {
        Stream<T> stream = parentFunction == null ? all.stream() : all.stream().filter(t -> parentFunction.apply(t) ==
                                                                                            (!DefaultTreeNode.DEFAULT_TYPE.equals(parent.getType()) ?
                                                                                             null : compareItemFunction.apply((T) parent.getData())));
        stream.forEach(t -> {
            DefaultTreeNode treeNode = new DefaultTreeNode(t, parent);
            treeNode.setExpanded(true);
            treeNode.setSelectable(nodeSelectable);
            if (parentFunction != null) {
                buildTree(treeNode, parentFunction, compareItemFunction, all, nodeSelectable);
            }
        });
    }

    default <T, U extends Comparable<? super U>, R> void buildTree(TreeNode parent, Function<T, R> parentFunction, Function<T, R> compareItemFunction,
                                                                   Function<T, U> sortFunction, List<T> all, Set<R> selectable) {
        Stream<T> stream = all.stream().filter(t -> parent.getData() == null ? parentFunction.apply(t) == null :
                                                    Objects.equals(parentFunction.apply(t), compareItemFunction.apply((T) parent.getData())));
        if (sortFunction != null) {
            stream = stream.sorted(Comparator.comparing(sortFunction));
        }
        stream.forEach(t -> {
            DefaultTreeNode treeNode = new DefaultTreeNode(t, parent);
            treeNode.setExpanded(true);
            if (selectable != null && !selectable.isEmpty()) {
                treeNode.setSelectable(selectable.contains(compareItemFunction.apply(t)));
            } else {
                treeNode.setSelectable(false);
            }
            buildTree(treeNode, parentFunction, compareItemFunction, sortFunction, all, selectable);
        });
    }

    default <T, R, K> void buildGroupingTree(TreeNode parent, Function<T, R> parentFunction, Function<T, R> compareItemFunction,
                                             Function<T, K> groupingFunction, Function<K, T> rootFunction, List<T> all, boolean nodeSelectable) {
        if (all.isEmpty()) {
            addEmptyMessage(parent, "No data");
        } else {
            all.stream().collect(Collectors.groupingBy(groupingFunction)).entrySet().forEach(entry -> {
                DefaultTreeNode treeNode = new DefaultTreeNode("group", rootFunction.apply(entry.getKey()), parent);
                treeNode.setExpanded(true);
                treeNode.setSelectable(nodeSelectable);
                buildTree(treeNode, parentFunction, compareItemFunction, entry.getValue(), nodeSelectable);
            });
        }
        streamTree(parent).filter(defaultTreeNode -> defaultTreeNode.getParent() != null && defaultTreeNode.getChildren().isEmpty())
                          .forEach(defaultTreeNode -> defaultTreeNode.setSelectable(true));
    }
}
