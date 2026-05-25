/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.reporttemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.dict.ReportTemplate;
import web.repository.dict.ReportTemplateRepository;
import web.view.model.AbstractVirtualScrollLazyModel;

@Getter
@Setter
@Log4j2
public class ReportTemplateModel extends AbstractVirtualScrollLazyModel<ReportTemplate, Long> {

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    private ReportTemplate selected;

    @Override
    protected Function<ReportTemplate, Long> keyFunction() {
        return ReportTemplate::getId;
    }

    @Override
    protected long count() {
        return reportTemplateRepository.count();
    }

    @Override
    protected List<ReportTemplate> loadData(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return reportTemplateRepository.findAll(null, first, pageSize);
    }
}
