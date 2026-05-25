/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.log.operation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.event.CloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import web.entity.ce.CurrencyOperation;
import web.entity.ce.Rule;
import web.repository.ce.CurrencyOperationRepository;
import web.repository.ce.RuleRepository;
import web.service.ce.OperationReportService;
import web.session.UserSession;

@Getter
@Setter
public class OperationShowView implements Serializable {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private CurrencyOperationRepository currencyOperationRepository;

    @Autowired
    private OperationReportService operationReportService;

    @Autowired
    private UserSession userSession;

    private CurrencyOperation operation;

    private String reportId;

    private String reportIdRequestConfirmationMoney;

    private String reportIdConsentPersonalData;

    private String reportIdMessageFinancialMonitoring;

    private boolean showReport;

    public void init(CurrencyOperation operation) {
        this.operation = operation;
    }

    public void openReport() {
        if (reportId == null) {
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            reportId = operationReportService.print(operation, userSession, locale);
            if (operation.getPersonHistory() != null) {
                BigDecimal sum = currencyOperationRepository
                        .getBaseAmountByPersonAndDate(operation.getPersonHistory().getPerson(), operation.getDate().with(LocalTime.MIDNIGHT),
                                                  operation.getDate().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1));
                Rule largeSum = ruleRepository.findBySystemNameAndSystem("LARGE_SUMS_CONTROL", true);
                Rule financialMonitoringSum = ruleRepository.findBySystemNameAndSystem("FIN_MON", true);
                if (sum.compareTo(largeSum.getMin()) >= 0) {
                    reportIdRequestConfirmationMoney = operationReportService.printRequestConfirmation(operation, locale);
                }
                if (sum.compareTo(financialMonitoringSum.getMin()) >= 0) {
                    reportIdMessageFinancialMonitoring = operationReportService.printMessageFinancialMonitoring(operation, userSession, locale);
                }
                reportIdConsentPersonalData = operationReportService.printConsentPersonalData(operation, userSession, locale);
            }
        }
        showReport = true;
    }

    public void closeReport(CloseEvent event) {
        showReport = false;
        ((Dialog) event.getComponent()).setVisible(true);
    }
}
