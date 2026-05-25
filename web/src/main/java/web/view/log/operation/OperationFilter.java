/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.log.operation;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import web.entity.core.Department;
import web.entity.core.Task;
import web.entity.core.User;
import web.entity.log.OperationCode;
import web.entity.log.OperationStatus;

@Data
public class OperationFilter implements Serializable, Cloneable {

    private String lastname;

    private String firstname;

    private String patronymic;

    private LocalDate birthdate;

    private String documentSeries;

    private String documentNumber;

    private Instant startDate;

    private Instant endDate;

    private List<OperationStatus> statuses = new ArrayList<>();

    private Task task;

    private List<OperationCode> codes = new ArrayList<>();

    private List<User> users = new ArrayList<>();

    private List<Department> departmentsByName = new ArrayList<>();

    private boolean extendedSearch;

    @Override
    public OperationFilter clone() {
        try {
            return (OperationFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
