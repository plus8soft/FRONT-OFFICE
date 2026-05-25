/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.faces.context.FacesContext;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public interface Tree {

    default <T> Set<T> filterTreeArray(TreeNode[] treeNodes, Predicate<TreeNode> predicate) {
        return mapTree(Stream.of(treeNodes).filter(predicate));
    }

    default <T> Set<T> mapTree(TreeNode[] treeNodes) {
        return mapTree(Stream.of(treeNodes));
    }

    default <T> Set<T> mapTree(Stream<TreeNode> stream) {
        return stream.map(treeNode -> (T) treeNode.getData()).collect(Collectors.toSet());
    }

    default <T, R> List<R> mapTree(TreeNode[] treeNodes, Function<T, R> function) {
        return Stream.of(treeNodes).map(treeNode -> function.apply((T) treeNode.getData())).collect(Collectors.toList());
    }

    default Stream<TreeNode> streamTree(TreeNode parentNode) {
        return parentNode.isLeaf() ? Stream.of(parentNode) :
               parentNode.getChildren().stream().map(this::streamTree).reduce(Stream.of(parentNode), Stream::concat);
    }

    default void addEmptyMessage(TreeNode treeNode, String message) {
        new DefaultTreeNode("empty", message, treeNode).setSelectable(false);
    }

    default void selectAllSelectable(Stream<TreeNode> stream) {
        stream.filter(node -> node.getParent() != null && node.isSelectable() && !node.isSelected()).forEach(node -> node.setSelected(true));
    }

    default void selectAllSelectable(TreeNode treeNode) {
        selectAllSelectable(streamTree(treeNode));
    }

    default void deselectAllSelected(Stream<TreeNode> stream) {
        stream.filter(TreeNode::isSelected).forEach(node -> node.setSelected(false));
    }

    default void deselectAllSelected(TreeNode treeNode) {
        deselectAllSelected(streamTree(treeNode));
    }

    default TreeNode[] getSelected(Stream<TreeNode> stream) {
        return stream.filter(TreeNode::isSelected).toArray(TreeNode[]::new);
    }

    default TreeNode[] getSelected(TreeNode treeNode) {
        return getSelected(streamTree(treeNode));
    }

    default TreeNode defineSourceNode(TreeNode treeNode, TreeDragDropEvent event) {
        Integer[] before = Stream.of(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                                                 .get(((org.primefaces.component.tree.Tree) event.getSource()).getClientId() + "_dragNode")
                                                 .split("_")).map(Integer::valueOf).toArray(Integer[]::new);
        Integer[] after = Stream.of(event.getDragNode().getRowKey().split("_")).map(Integer::valueOf).toArray(Integer[]::new);
        if (after.length < before.length && after[after.length - 1] <= before[after.length - 1] &&
            !IntStream.range(0, after.length - 1).filter(i -> !after[i].equals(before[i])).findFirst().isPresent()) {
            before[after.length - 1] = before[after.length - 1] + 1;
        }
        return Stream.of(before).limit(before.length - 1).reduce(treeNode, (node, index) -> node.getChildren().get(index), (parent, node) -> node);
    }
}
