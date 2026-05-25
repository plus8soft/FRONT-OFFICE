/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.ce.clienthistory;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import web.entity.crm.Change;
import web.entity.crm.ChangeLog;
import web.entity.crm.ChangeLog_;
import web.entity.crm.Change_;
import web.entity.crm.Person;
import web.repository.crm.ChangeLogRepository;
import web.repository.crm.ChangeRepository;

@Getter
@Setter
@Log4j2
public class ClientHistoryView implements Serializable {

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private ChangeLogRepository changeLogRepository;

    private List<Change> changes;

    private Change selected;

    private List<ChangeLog> changeLogs;

    public void init(Person person) {
        changes = changeRepository
                .findAll((root, query, cb) -> cb.equal(root.get(Change_.person), person), new Sort(Sort.Direction.DESC, Change_.dateTime.getName()));
    }

    public void onSelect() {
        changeLogs = changeLogRepository.findAll((root, query, cb) -> cb.equal(root.get(ChangeLog_.change), selected));
    }
}
