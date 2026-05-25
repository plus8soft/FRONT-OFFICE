/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.documenttype;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.DocumentType;
import web.repository.dict.DocumentTypeRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class DocumentTypeModel extends AbstractVirtualScrollLazyModel<DocumentType, Long> {

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private DocumentType selected;

    @Override
    protected Function<DocumentType, Long> keyFunction() {
        return DocumentType::getId;
    }

    @Override
    protected long count() {
        return documentTypeRepository.count();
    }

    @Override
    public List<DocumentType> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return documentTypeRepository.findAll(null, first, pageSize);
    }
}
