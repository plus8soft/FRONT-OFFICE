/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.rule;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.ce.Rule;
import web.repository.ce.RuleParameterRepository;
import web.repository.ce.RuleRepository;
import web.view.Message;

@Getter
@Setter
@Log4j2
public class RuleView implements Message, Serializable {

    @Autowired
    private RuleParameterRepository ruleParameterRepository;

    @Autowired
    private transient RuleRepository ruleRepository;

    private List<Rule> systemRules;

    private Rule selectedSystemRule;

    private List<Rule> conversionRules;

    private Rule selectedConversionRule;

    private List<Rule> commissionRules;

    private Rule selectedCommissionRule;

    public void init() {
        List<Rule> rules = ruleRepository.findAll();
        systemRules = rules.stream().filter(Rule::isSystem).collect(Collectors.toList());
        conversionRules = rules.stream().filter(Rule::isCurrency).sorted(Comparator.comparing(Rule::getPosition)).collect(Collectors.toList());
        commissionRules = rules.stream().filter(Rule::isCommision).collect(Collectors.toList());
    }

    public Rule add(boolean currency, boolean commission) {
        Rule rule = new Rule();
        rule.setCurrency(currency);
        rule.setCommision(commission);
        return rule;
    }

    public Rule edit(Rule rule) {
        Rule editRule = new Rule();
        BeanUtils.copyProperties(rule, editRule);
        return editRule;
    }

    public void deleteRule(Rule rule) {
        try {
            deleteRuleWithParams(rule);
            if (rule.equals(selectedConversionRule)) {
                selectedConversionRule = null;
            } else {
                selectedCommissionRule = null;
            }
            init();
            addInfoMessage("Data saved successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }

    private void deleteRuleWithParams(Rule rule) {
        try {
            ruleRepository.delete(rule);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }

    public void onRowReorder() {
        try {
            IntStream.range(0, conversionRules.size()).forEach(position -> conversionRules.get(position).setPosition(position));
            ruleRepository.save(conversionRules);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addErrorMessage("Internal error while saving data.");
        }
    }
}
