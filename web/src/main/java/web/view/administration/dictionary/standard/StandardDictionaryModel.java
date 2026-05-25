/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import web.entity.core.Dictionary;
import web.entity.core.DictionaryName;
import web.entity.core.DictionaryValue;
import web.entity.core.DictionaryValue_;
import web.entity.core.Dictionary_;
import web.repository.core.DictionaryRepository;
import web.repository.core.DictionaryValueRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class StandardDictionaryModel extends AbstractVirtualScrollLazyModel<StandardDictionaryItem, DictionaryName> {

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private DictionaryValueRepository dictionaryValueRepository;

    private StandardDictionaryItem selected;

    private StandardDictionaryFilter filter;

    @Override
    protected Function<StandardDictionaryItem, DictionaryName> keyFunction() {
        return standardDictionaryItem -> standardDictionaryItem.getDictionary().getId();
    }

    @Override
    protected long count() {
        return dictionaryRepository.count(getSpecification());
    }

    @Override
    public List<StandardDictionaryItem> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return dictionaryRepository.findAll(getSpecification(), first, pageSize).stream().map(dictionary -> {
            StandardDictionaryItem standardDictionaryItem = new StandardDictionaryItem();
            standardDictionaryItem.setDictionary(dictionary);
            standardDictionaryItem
                    .setCount(dictionaryValueRepository.count((root, query, cb) -> cb.equal(root.get(DictionaryValue_.dictionary), dictionary)));
            return standardDictionaryItem;
        }).collect(Collectors.toList());
    }

    private Specification<Dictionary> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getName())) {
                predicates.add(cb.like(cb.upper(root.get(Dictionary_.name)), String.format("%s%%", filter.getName().toUpperCase())));
            }
            if (filter.isExtendedSearch()) {
                if (Objects.nonNull(filter.getGroup())) {
                    predicates.add(cb.equal(root.get(Dictionary_.group), filter.getGroup()));
                }
                if (Objects.nonNull(filter.getUpdateDateWith())) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(Dictionary_.updateDate), filter.getUpdateDateWith()));
                }
                if (Objects.nonNull(filter.getUpdateDate())) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(Dictionary_.updateDate), filter.getUpdateDate()));
                }
                if (Objects.nonNull(filter.getCode()) || Objects.nonNull(filter.getShortValue()) || Objects.nonNull(filter.getValue())) {
                    Join<Dictionary, DictionaryValue> join = root.join(Dictionary_.dictionaryValues);
                    if (Objects.nonNull(filter.getCode())) {
                        predicates.add(cb.like(cb.upper(join.get(DictionaryValue_.code)), String.format("%s%%", filter.getCode().toUpperCase())));
                    }
                    if (Objects.nonNull(filter.getShortValue())) {
                        predicates.add(cb.like(cb.upper(join.get(DictionaryValue_.shortValue)),
                                               String.format("%s%%", filter.getShortValue().toUpperCase())));
                    }
                    if (Objects.nonNull(filter.getValue())) {
                        predicates.add(cb.like(cb.upper(join.get(DictionaryValue_.value)), String.format("%s%%", filter.getValue().toUpperCase())));
                    }
                }
            }
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
