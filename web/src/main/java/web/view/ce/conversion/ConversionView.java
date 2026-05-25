/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.conversion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import web.entity.ce.DepartmentCurrency;
import web.entity.ce.DepartmentCurrency_;
import web.entity.ce.Rule;
import web.entity.ce.RuleParameter;
import web.entity.ce.RuleParameter_;
import web.entity.ce.Rule_;
import web.entity.ce.Sign;
import web.entity.core.Department;
import web.entity.dict.Currency;
import web.entity.dict.Currency_;
import web.repository.ce.DepartmentCurrencyRepository;
import web.repository.ce.RuleParameterRepository;
import web.repository.ce.RuleRepository;
import web.repository.dict.CurrencyRepository;
import web.session.UserSession;
import web.view.DefaultTree;
import web.view.Message;
import web.view.ce.MergeProperty;
import web.view.ce.item.DepartmentItem;
import web.view.ce.item.RuleItem;

@Getter
@Setter
@Log4j2
public class ConversionView implements MergeProperty, Message, Serializable, DefaultTree {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private DepartmentCurrencyRepository departmentCurrencyRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleParameterRepository ruleParameterRepository;

    @Autowired
    private UserSession userSession;

    private TreeNode departmentTree;

    private TreeNode[] selectedDepartments;

    private Map<Department, List<DepartmentCurrency>> departmentCurrencyIndex;

    private List<Currency> allCurrencies;

    private List<Currency> currencies;

    private Currency selectedCurrency;

    private List<Rule> rules;

    private List<RuleItem> ruleItems;

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
        allCurrencies = currencyRepository.findAll(new Sort(Currency_.id.getName()));
        currencies = new ArrayList<>();
        rules = ruleRepository
                .findAll((root, query, cb) -> cb.and(cb.equal(root.get(Rule_.enabled), true), cb.equal(root.get(Rule_.currency), true)));
        ruleItems = new ArrayList<>();
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
        currencies.clear();
        if (selectedDepartments.length != 0) {
            Stream<Currency> currencyStream = allCurrencies.stream();
            for (Department department : mapTree(selectedDepartments, DepartmentItem::getDepartment)) {
                List<Currency> currencies =
                        departmentCurrencyIndex.get(department).stream().map(DepartmentCurrency::getCurrency).collect(Collectors.toList());
                currencyStream = currencyStream.filter(currencies::contains);
            }
            currencies.addAll(currencyStream.sorted(Comparator.comparing(Currency::getPosition)).collect(Collectors.toList()));
        }
        selectedCurrency = null;
        onChangeCurrency();
    }

    public void onChangeCurrency(SelectEvent event) {
        selectedCurrency = (Currency) event.getObject();
        onChangeCurrency();
    }

    @Transactional
    public void onChangeCurrency() {
        edited = false;
        ruleItems.clear();
        if (selectedCurrency != null) {
            List<RuleParameter> ruleParams = findRuleParams(mapTree(this.selectedDepartments, DepartmentItem::getDepartment));
            for (Rule rule : rules) {
                RuleItem ruleItem = new RuleItem();
                ruleItem.setRule(rule);
                mergeRuleParams(ruleParams.stream().filter(ruleParameter -> rule.equals(ruleParameter.getRule())).collect(Collectors.toList()),
                                ruleItem);
                if (Objects.isNull(ruleItem.getRuleParameter().getBuySign())) {
                    ruleItem.getRuleParameter().setBuySign(Sign.MINUS);
                }
                ruleItems.add(ruleItem);
            }
            ruleItems.sort(Comparator.comparing(ruleItem -> ruleItem.getRule().getPosition()));
        }
    }

    private List<RuleParameter> findRuleParams(List<Department> departments) {
        List<RuleParameter> ruleParams = ruleParameterRepository.findAll((root, query, cb) -> cb
                .and(cb.equal(root.get(RuleParameter_.currency), selectedCurrency), root.get(RuleParameter_.department).in(departments)));
        for (Department department : departments) {
            rules.stream().filter(rule -> ruleParams.stream().noneMatch(
                    ruleParameter -> rule.equals(ruleParameter.getRule()) && department.equals(ruleParameter.getDepartment())))
                 .forEach(rule -> ruleParams.add(RuleParameter.builder().rule(rule).department(department).build()));
        }
        return ruleParams;
    }

    private List<FacesMessage> validateView() {
        List<FacesMessage> result = new ArrayList<>();
        List<Rule> enabledRules = ruleItems.stream().filter(ruleItem -> !ruleItem.isEnabledConflict() && ruleItem.getRuleParameter().getEnabled())
                                           .map(RuleItem::getRule).collect(Collectors.toList());
        ruleItems.forEach(ruleItem -> {
            if (ruleItem.isEnabledConflict()) {
                result.add(errorMessageFormated("Rule \"{0}\". Conflict in \"Rule Active\" field", ruleItem.getRule().getName()));
            }
            if (ruleItem.isSellConflict()) {
                result.add(errorMessageFormated("Rule \"{0}\". Conflict in \"Sell\" field", ruleItem.getRule().getName()));
            }
            if (ruleItem.isBuyConflict()) {
                result.add(errorMessageFormated("Rule \"{0}\". Conflict in \"Buy\" field", ruleItem.getRule().getName()));
            }
            if (!ruleItem.isEnabledConflict() && isRangesOverlap(ruleItem, enabledRules)) {
                result.add(errorMessageFormated("Rule ranges overlap.", ruleItem.getRule().getName()));
            }
        });
        return result;
    }

    private boolean isRangesOverlap(RuleItem ruleItem, List<Rule> enabledRules) {
        return ruleItem.getRuleParameter().getEnabled() &&
               ruleRepository.findAll().stream().filter(rule -> enabledRules.contains(rule) && !rule.equals(ruleItem.getRule()) && rule.isCurrency())
                             .anyMatch(enabledRule -> (Objects.isNull(enabledRule.getMax()) ||
                                                       ruleItem.getRule().getMin().compareTo(enabledRule.getMax()) != 1) &&
                                                      (Objects.isNull(ruleItem.getRule().getMax()) ||
                                                       enabledRule.getMin().compareTo(ruleItem.getRule().getMax()) != 1));
    }

    public void saveView() {
        List<FacesMessage> messages = validateView();
        if (!messages.isEmpty()) {
            messages.stream().limit(5).forEach(message -> addMessage(null, message));
        } else {
            try {
                save();
                addInfoMessage("Data saved successfully.");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addErrorMessage("Internal error while saving data.");
            }
        }
    }

    @Transactional
    private void save() {
        List<Department> selectedDepartments = mapTree(this.selectedDepartments, DepartmentItem::getDepartment);
        ruleParameterRepository.deleteAllByDepartmentInAndCurrency(selectedDepartments, selectedCurrency);
        List<RuleParameter> ruleParameters = new ArrayList<>();
        for (Department department : selectedDepartments) {
            for (RuleItem ruleItem : ruleItems) {
                RuleParameter ruleParameter = new RuleParameter();
                BeanUtils.copyProperties(ruleItem.getRuleParameter(), ruleParameter);
                ruleParameter.setDepartment(department);
                ruleParameter.setRule(ruleItem.getRule());
                ruleParameter.setCurrency(selectedCurrency);
                ruleParameters.add(ruleParameter);
            }
        }
        ruleParameterRepository.save(ruleParameters);
    }
}
