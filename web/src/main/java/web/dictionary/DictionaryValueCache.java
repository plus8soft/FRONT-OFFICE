/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.dictionary;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import web.entity.core.DictionaryName;
import web.entity.core.DictionaryValue;
import web.entity.core.DictionaryValue_;
import web.entity.core.Dictionary_;
import web.repository.core.DictionaryValueRepository;

@Service
public class DictionaryValueCache {

    @Autowired
    private DictionaryValueRepository dictionaryValueRepository;

    @Cacheable(value = "dictionary-unit", key = "{#dictionaryName, #code}", sync = true)
    public <K> Unit<K> findOne(DictionaryName dictionaryName, Function<String, K> keyFunction, K code) {
        return findAll(dictionaryName, keyFunction).stream().filter(kUnit -> kUnit.getCode().equals(code)).findFirst().orElse(null);
    }

    @Cacheable(value = "dictionary", key = "#dictionaryName", sync = true)
    public <K> List<Unit<K>> findAll(DictionaryName dictionaryName, Function<String, K> keyFunction) {
        return dictionaryValueRepository
                .findAll((root, query, cb) -> cb.equal(root.get(DictionaryValue_.dictionary).get(Dictionary_.id), dictionaryName)).stream()
                .map(dictionaryValue -> new Unit<>(keyFunction.apply(dictionaryValue.getCode()), dictionaryValue.getValue(),
                                                   dictionaryValue.getShortValue(), dictionaryValue.isUnused())).collect(Collectors.toList());
    }

    public <K> List<Unit<K>> findAll(DictionaryName dictionaryName, Function<String, K> keyFunction, K... exclusions) {
        Set<K> exclusionList = Stream.of(exclusions).collect(Collectors.toSet());
        return findAll(dictionaryName, keyFunction).stream().filter(unit -> !exclusionList.contains(unit.getCode())).collect(Collectors.toList());
    }

    @Caching(evict = {@CacheEvict(value = "dictionary-unit", allEntries = true),
                      @CacheEvict(value = "dictionary", key = "#dictionaryValue.dictionary.id")})
    public void delete(DictionaryValue dictionaryValue) {
        dictionaryValueRepository.delete(dictionaryValue);
    }

    @Caching(evict = {@CacheEvict(value = "dictionary-unit", allEntries = true),
                      @CacheEvict(value = "dictionary", key = "#dictionaryValue.dictionary.id")})
    public void save(DictionaryValue dictionaryValue) {
        dictionaryValueRepository.save(dictionaryValue);
    }
}
