/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import web.entity.ce.Rate;
import web.entity.ce.RuleParameter;
import web.view.ce.item.RateItem;
import web.view.ce.item.RuleItem;

public interface MergeProperty {

    default <T, R> R mergeProperty(Function<T, R> getter, Collection<T> collection) throws MergePropertyException {
        Set<R> mergeResult = collection.stream().map(getter).collect(Collectors.toSet());
        if (mergeResult.size() == 1) {
            return mergeResult.iterator().next();
        } else {
            throw new MergePropertyException();
        }
    }

    default void mergeRuleParams(List<RuleParameter> ruleParams, RuleItem ruleItem) {
        ruleItem.setRuleParameter(new RuleParameter());
        try {
            ruleItem.getRuleParameter().setEnabled(mergeProperty(RuleParameter::getEnabled, ruleParams));
        } catch (MergePropertyException e) {
            ruleItem.setEnabledConflict(true);
        }
        try {
            ruleItem.getRuleParameter().setSellSign(mergeProperty(RuleParameter::getSellSign, ruleParams));
            ruleItem.getRuleParameter().setSellPercent(mergeProperty(RuleParameter::getSellPercent, ruleParams));
            ruleItem.getRuleParameter().setSellValue(mergeProperty(RuleParameter::getSellValue, ruleParams));
        } catch (MergePropertyException e) {
            ruleItem.getRuleParameter().setSellSign(null);
            ruleItem.getRuleParameter().setSellPercent(null);
            ruleItem.getRuleParameter().setSellValue(null);
            ruleItem.setSellConflict(true);
        }
        try {
            ruleItem.getRuleParameter().setBuySign(mergeProperty(RuleParameter::getBuySign, ruleParams));
            ruleItem.getRuleParameter().setBuyPercent(mergeProperty(RuleParameter::getBuyPercent, ruleParams));
            ruleItem.getRuleParameter().setBuyValue(mergeProperty(RuleParameter::getBuyValue, ruleParams));
        } catch (MergePropertyException e) {
            ruleItem.getRuleParameter().setBuySign(null);
            ruleItem.getRuleParameter().setBuyPercent(null);
            ruleItem.getRuleParameter().setBuyValue(null);
            ruleItem.setBuyConflict(true);
        }
    }

    default void mergeRates(List<Rate> rates, RateItem rateItem) {
        rateItem.setRate(new Rate());
        try {
            rateItem.getRate().setSellRate(mergeProperty(Rate::getSellRate, rates));
        } catch (MergePropertyException e) {
            rateItem.setSellConflict(true);
        }
        try {
            rateItem.getRate().setBuyRate(mergeProperty(Rate::getBuyRate, rates));
        } catch (MergePropertyException e) {
            rateItem.setBuyConflict(true);
        }
    }
}
