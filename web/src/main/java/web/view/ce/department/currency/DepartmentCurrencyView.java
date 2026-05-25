/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.department.currency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.ce.DepartmentCurrency;
import web.entity.ce.DepartmentCurrency_;
import web.entity.core.Department;
import web.entity.dict.Currency;
import web.repository.ce.DepartmentCurrencyRepository;
import web.repository.ce.RuleParameterRepository;
import web.repository.dict.CurrencyRepository;
import web.session.UserSession;
import web.view.DefaultTree;
import web.view.Message;
import web.view.ce.item.DepartmentItem;

@Getter
@Setter
@Log4j2
public class DepartmentCurrencyView implements Message, Serializable, DefaultTree {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private DepartmentCurrencyRepository departmentCurrencyRepository;

    @Autowired
    private RuleParameterRepository ruleParameterRepository;

    @Autowired
    private UserSession userSession;

    private TreeNode departmentTree;

    private TreeNode[] selectedDepartments;

    private Map<Department, List<DepartmentCurrency>> departmentCurrencyIndex;

    private List<Currency> currencies;

    private Set<Currency> availableCurrencies;

    private List<Currency> selectedAvailableCurrencies;

    private List<Currency> targetCurrencies;

    private List<Currency> selectedTargetCurrencies;

    private Boolean edited;

    @Transactional
    public void init() {
        edited = false;
        List<Department> departments = userSession.getDepartmentsGraph();
        buildTree(departmentTree = new DefaultTreeNode(), departmentItem -> departmentItem.getDepartment().getParent(), DepartmentItem::getDepartment,
                  null, departments.stream().map(department -> DepartmentItem.builder().department(department).build()).collect(Collectors.toList()),
                  userSession.getDepartments());
        departmentCurrencyIndex =
                departmentCurrencyRepository.findAll((root, query, cb) -> root.get(DepartmentCurrency_.department).in(departments)).stream()
                                            .collect(Collectors.groupingBy(DepartmentCurrency::getDepartment));
        departments.forEach(department -> {
            if (!departmentCurrencyIndex.containsKey(department)) {
                departmentCurrencyIndex.put(department, new ArrayList<>());
            }
        });
        currencies = currencyRepository.findAll();
        availableCurrencies = new TreeSet<>(Comparator.comparing(Currency::getPosition));
        selectedAvailableCurrencies = new ArrayList<>();
        targetCurrencies = new ArrayList<>();
        selectedTargetCurrencies = new ArrayList<>();
    }

    public void toTargetCurrencies() {
        moveCurrencies(selectedAvailableCurrencies, availableCurrencies, targetCurrencies, selectedAvailableCurrencies, selectedTargetCurrencies);
    }

    public void toAvailableCurrencies() {
        moveCurrencies(selectedTargetCurrencies, targetCurrencies, availableCurrencies, selectedTargetCurrencies, selectedAvailableCurrencies);
    }

    public void toTargetAllCurrencies() {
        moveCurrencies(availableCurrencies, availableCurrencies, targetCurrencies, selectedAvailableCurrencies, selectedTargetCurrencies);
    }

    public void toAvailableAllCurrencies() {
        moveCurrencies(targetCurrencies, targetCurrencies, availableCurrencies, selectedTargetCurrencies, selectedAvailableCurrencies);
    }

    private void moveCurrencies(Collection<Currency> currencies, Collection<Currency> from, Collection<Currency> to,
                                Collection<Currency> selectedFrom, Collection<Currency> selectedTo) {
        to.addAll(currencies);
        selectedTo.clear();
        selectedTo.addAll(currencies);
        from.removeAll(currencies);
        selectedFrom.clear();
        edited = true;
    }

    public void moveCurrencyUp() {
        if (selectedTargetCurrencies.size() == 1) {
            int index = targetCurrencies.indexOf(selectedTargetCurrencies.get(0));
            if (index != 0) {
                Collections.swap(targetCurrencies, index, index - 1);
                edited = true;
            }
        }
    }

    public void moveCurrencyDown() {
        if (selectedTargetCurrencies.size() == 1) {
            int index = targetCurrencies.indexOf(selectedTargetCurrencies.get(0));
            if (index != targetCurrencies.size() - 1) {
                Collections.swap(targetCurrencies, index, index + 1);
                edited = true;
            }
        }
    }

    public void selectAllDepartments() {
        selectAllSelectable(departmentTree);
        int oldLength = selectedDepartments.length;
        selectedDepartments = getSelected(streamTree(departmentTree).skip(1));
        if (oldLength != selectedDepartments.length) {
            onChangeDepartmentTree();
        }
    }

    public void deselectAllDepartments() {
        selectedDepartments = new TreeNode[0];
        deselectAllSelected(departmentTree);
        onChangeDepartmentTree();
    }

    public void selectAllChilds() {
        String rowKey = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowKey");
        streamTree(departmentTree).filter(treeNode -> rowKey.equals(treeNode.getRowKey()) && !treeNode.isLeaf()).findFirst().ifPresent(treeNode -> {
            selectAllSelectable(streamTree(treeNode).skip(1));
            int oldLength = selectedDepartments.length;
            selectedDepartments = getSelected(streamTree(departmentTree).skip(1));
            if (oldLength != selectedDepartments.length) {
                onChangeDepartmentTree();
            }
        });
    }

    public void deselectAllChilds() {
        String rowKey = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowKey");
        streamTree(departmentTree).filter(treeNode -> rowKey.equals(treeNode.getRowKey()) && !treeNode.isLeaf()).findFirst().ifPresent(treeNode -> {
            deselectAllSelected(streamTree(treeNode).skip(1));
            int oldLength = selectedDepartments.length;
            selectedDepartments = getSelected(streamTree(departmentTree).skip(1));
            if (oldLength != selectedDepartments.length) {
                onChangeDepartmentTree();
            }
        });
    }

    public void onChangeDepartmentTree() {
        edited = false;
        availableCurrencies.clear();
        targetCurrencies.clear();
        selectedAvailableCurrencies.clear();
        selectedTargetCurrencies.clear();
        if (selectedDepartments.length != 0) {
            List<Department> selectedDepartments = mapTree(this.selectedDepartments, DepartmentItem::getDepartment);
            Stream<Currency> currencyStream = currencies.stream();
            for (Department department : selectedDepartments) {
                List<Currency> currencies =
                        departmentCurrencyIndex.get(department).stream().map(DepartmentCurrency::getCurrency).collect(Collectors.toList());
                currencyStream = currencyStream.filter(currencies::contains);
            }
            List<Currency> currencies =
                    departmentCurrencyIndex.get(selectedDepartments.get(0)).stream().sorted(Comparator.comparing(DepartmentCurrency::getPosition))
                                           .map(DepartmentCurrency::getCurrency).collect(Collectors.toList());
            targetCurrencies.addAll(currencyStream.sorted((o1, o2) -> Integer.valueOf(currencies.indexOf(o1)).compareTo(currencies.indexOf(o2)))
                                                  .collect(Collectors.toList()));
            availableCurrencies.addAll(this.currencies.stream().filter(currency -> !targetCurrencies.contains(currency) && currency.isEnabled())
                                                      .collect(Collectors.toSet()));
        }
    }

    public void saveView() {
        try {
            save();
            onChangeDepartmentTree();
            addInfoMessage("Data saved successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }

    @Transactional
    public void save() {
        List<Department> selectedDepartments = mapTree(this.selectedDepartments, DepartmentItem::getDepartment);
        departmentCurrencyRepository.delete(selectedDepartments.stream().flatMap(department -> departmentCurrencyIndex.get(department).stream())
                                                               .collect(Collectors.toList()));
        selectedDepartments.forEach(department -> {
            List<DepartmentCurrency> deletedDepartmentCurrencies = departmentCurrencyIndex.get(department).stream()
                                                                                          .filter(departmentCurrency -> !targetCurrencies
                                                                                                  .contains(departmentCurrency.getCurrency()))
                                                                                          .collect(Collectors.toList());
            deletedDepartmentCurrencies.forEach(departmentCurrency -> ruleParameterRepository
                    .deleteAllByDepartmentAndCurrency(departmentCurrency.getDepartment(), departmentCurrency.getCurrency()));
            departmentCurrencyRepository.delete(deletedDepartmentCurrencies);
            departmentCurrencyIndex.get(department).clear();
            departmentCurrencyIndex.get(department).addAll(IntStream.range(0, targetCurrencies.size()).mapToObj(
                    i -> DepartmentCurrency.builder().department(department).currency(targetCurrencies.get(i)).position(i).build())
                                                                    .collect(Collectors.toList()));
            departmentCurrencyRepository.save(departmentCurrencyIndex.get(department));
        });
    }
}
