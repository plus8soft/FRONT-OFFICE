/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.standard.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import web.entity.core.DictionaryValue;
import web.entity.core.DictionaryValue_;
import web.repository.core.DictionaryValueRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class DictionaryValueModel extends AbstractVirtualScrollLazyModel<DictionaryValue, Long> {

    @Autowired
    private DictionaryValueRepository dictionaryValueRepository;

    private DictionaryValue selected;

    private DictionaryValueFilter filter;

    @Override
    protected Function<DictionaryValue, Long> keyFunction() {
        return DictionaryValue::getId;
    }

    @Override
    protected long count() {
        return dictionaryValueRepository.count(getSpecification());
    }

    @Override
    public List<DictionaryValue> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return dictionaryValueRepository.findAll(getSpecification(), first, pageSize);
    }

    private Specification<DictionaryValue> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(DictionaryValue_.dictionary), filter.getDictionary()));
            if (Objects.nonNull(filter.getTextFilter())) {
                String text = String.format("%s%%", filter.getTextFilter().toUpperCase());
                predicates.add(cb.or(cb.like(cb.upper(root.get(DictionaryValue_.code)), text),
                                     cb.or(cb.like(cb.upper(root.get(DictionaryValue_.shortValue)), text),
                                           cb.like(cb.upper(root.get(DictionaryValue_.value)), text))));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
