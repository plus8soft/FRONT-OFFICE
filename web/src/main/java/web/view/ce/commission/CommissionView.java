/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.commission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import web.entity.ce.DepartmentCurrency_;
import web.entity.ce.Rule;
import web.entity.ce.RuleParameter;
import web.entity.ce.RuleParameter_;
import web.entity.ce.Rule_;
import web.entity.ce.Sign;
import web.entity.core.Department;
import web.repository.ce.DepartmentCurrencyRepository;
import web.repository.ce.RuleParameterRepository;
import web.repository.ce.RuleRepository;
import web.session.UserSession;
import web.view.DefaultTree;
import web.view.Message;
import web.view.ce.MergeProperty;
import web.view.ce.item.DepartmentItem;
import web.view.ce.item.RuleItem;

@Getter
@Setter
@Log4j2
public class CommissionView implements MergeProperty, Message, Serializable, DefaultTree {

    private static final TreeNode[] EMPTY_ARRAY = new TreeNode[0];

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleParameterRepository ruleParameterRepository;

    @Autowired
    private DepartmentCurrencyRepository departmentCurrencyRepository;

    @Autowired
    private UserSession userSession;

    private TreeNode departmentTree;

    private TreeNode[] selectedDepartments;

    private List<Rule> rules;

    private List<RuleItem> ruleItems;

    private Boolean edited;

    private Function<DepartmentItem, Department> function = DepartmentItem::getDepartment;

    @Transactional
    public void init() {
        edited = false;
        buildTree(departmentTree = new DefaultTreeNode(), function.andThen(Department::getParent), function, null,
                  userSession.getDepartmentsGraph().stream().map(department -> {
                      DepartmentItem departmentItem = new DepartmentItem();
                      departmentItem.setDepartment(department);
                      departmentItem.setCurrenciesCount(departmentCurrencyRepository.count((root, query, cb) -> cb
                              .equal(root.get(DepartmentCurrency_.department), department)));
                      return departmentItem;
                  }).collect(Collectors.toList()), userSession.getDepartments());
        rules = ruleRepository
                .findAll((root, query, cb) -> cb.and(cb.equal(root.get(Rule_.enabled), true), cb.equal(root.get(Rule_.commision), true)));
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
        selectedDepartments = EMPTY_ARRAY;
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
        ruleItems.clear();
        if (selectedDepartments.length != 0) {
            List<RuleParameter> ruleParameters = findRuleParameters(mapTree(selectedDepartments, function));
            for (Rule rule : rules) {
                RuleItem ruleItem = new RuleItem();
                ruleItem.setRule(rule);
                mergeRuleParams(ruleParameters.stream().filter(ruleParameter -> rule.equals(ruleParameter.getRule())).collect(Collectors.toList()),
                                ruleItem);
                ruleItems.add(ruleItem);
            }
        }
    }

    private List<RuleParameter> findRuleParameters(List<Department> departments) {
        List<RuleParameter> ruleParams = ruleParameterRepository.findAll(
                (root, query, cb) -> cb.and(root.get(RuleParameter_.department).in(departments), cb.isNull(root.get(RuleParameter_.currency))));
        for (Department department : departments) {
            for (Rule rule : rules) {
                if (ruleParams.stream()
                              .noneMatch(ruleParameter -> rule.equals(ruleParameter.getRule()) && department.equals(ruleParameter.getDepartment()))) {
                    ruleParams.add(RuleParameter.builder().rule(rule).department(department).build());
                }
            }
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
            if (!ruleItem.isEnabledConflict() && ruleItem.getRuleParameter().getEnabled()) {
                if (ruleItem.getRuleParameter().getBuyValue() == null && ruleItem.getRuleParameter().getBuyPercent() == null) {
                    result.add(errorMessageFormated("Rule \"{0}\". \"Buy\" field must be filled", ruleItem.getRule().getName()));
                }
                if (ruleItem.getRuleParameter().getSellValue() == null && ruleItem.getRuleParameter().getSellPercent() == null) {
                    result.add(errorMessageFormated("Rule \"{0}\". \"Sell\" field must be filled", ruleItem.getRule().getName()));
                }
                if (isRangesOverlap(ruleItem, enabledRules)) {
                    result.add(errorMessageFormated("Rule ranges overlap.", ruleItem.getRule().getName()));
                }
            }
        });
        return result;
    }

    private boolean isRangesOverlap(RuleItem ruleItem, List<Rule> enabledRules) {
        return ruleRepository.findAll().stream().filter(rule -> enabledRules.contains(rule) && !rule.equals(ruleItem.getRule()) && rule.isCommision())
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
        List<Department> selectedDepartments = mapTree(this.selectedDepartments, function);
        ruleParameterRepository.deleteAllByDepartmentInAndCurrencyIsNull(selectedDepartments);
        List<RuleParameter> ruleParams = new ArrayList<>();
        for (Department department : selectedDepartments) {
            for (RuleItem ruleItem : ruleItems) {
                RuleParameter ruleParameter = new RuleParameter();
                BeanUtils.copyProperties(ruleItem.getRuleParameter(), ruleParameter);
                ruleParameter.setDepartment(department);
                ruleParameter.setRule(ruleItem.getRule());
                ruleParameter.setBuySign(Sign.PLUS);
                ruleParameter.setSellSign(Sign.PLUS);
                ruleParams.add(ruleParameter);
            }
        }
        ruleParameterRepository.save(ruleParams);
    }
}
