/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.audit.ce;

import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import web.audit.EventAspect;
import web.dictionary.DocumentTypeDictionary;
import web.entity.core.EventCode;
import web.entity.core.EventStatus;
import web.entity.log.OperationCode;
import web.session.Menu;
import web.view.ce.clientedit.EditClientView;
import web.view.ce.clientsearch.ClientSearchView;
import web.view.ce.currencyexchange.StepFour;
import web.view.ce.currencyexchange.StepTwo;

@Aspect
@Configurable
public class CurrencyOperationAspect extends EventAspect {

    private static final String CE_MENU = "menu-single-window-currency-exchange";

    @Autowired
    private DocumentTypeDictionary documentTypeDictionary;

    @Pointcut("execution(java.lang.String web.view.ce.currencyexchange.StepOne.calculate())")
    public static void group() {
    }

    @Pointcut("if() && execution(void web.view.ce.currencyexchange.StepTwo.init(web.entity.ce.CurrencyOperation))")
    public static boolean calculatingAmountExchange() {
        return isEnabled(EventCode.CE_CALCULATING_AMOUNT_EXCHANGE);
    }

    @Pointcut("if() && execution(java.lang.String web.view.ce.currencyexchange.StepTwo.cancel())")
    public static boolean cancelingOperation() {
        return isEnabled(EventCode.CE_CANCELING_OPERATION);
    }

    @Pointcut("if() && execution(void web.view.ce.currencyexchange.StepFour.init(web.entity.ce.CurrencyOperation, java.lang.Long))")
    public static boolean operationStarted() {
        return isEnabled(EventCode.CE_OPERATION_STARTED);
    }

    @Pointcut("if() && execution(void web.view.ce.currencyexchange.StepFour.openReport())")
    public static boolean reportReprint() {
        return isEnabled(EventCode.CE_REPORT_REPRINT);
    }

    @Pointcut("if() && execution(java.lang.String web.view.ce.currencyexchange.StepFour.cancel())")
    public static boolean operationComplete() {
        return isEnabled(EventCode.CE_OPERATION_COMPLETE);
    }

    @Pointcut("if() && (execution(String web.view.ce.clientsearch.ClientSearchView.onSelectPersonDefine(web.projection.PersonAutoComplete)) || " +
              "execution(java.lang.String web.view.ce.clientsearch.ClientSearchView.findPerson()))")
    public static boolean personSearch() {
        return CE_MENU.equals(Menu.getInstance().getTask().getSystemName()) && isEnabled(EventCode.CE_PERSON_SEARCH);
    }

    @Pointcut("if() && execution(String web.view.ce.clientsearch.ClientSearchView.addPerson())")
    public static boolean personAddition() {
        return CE_MENU.equals(Menu.getInstance().getTask().getSystemName()) && isEnabled(EventCode.CE_ADDING_PERSON);
    }

    @Pointcut("if() && execution(String web.view.ce.clientedit.EditClientView.next())")
    public static boolean personCreatedOrModified() {
        return CE_MENU.equals(Menu.getInstance().getTask().getSystemName());
    }

    @Pointcut("if() && execution(String web.view.ce.clientsearch.ClientSearchView.toEditClient())")
    public static boolean personModification() {
        return CE_MENU.equals(Menu.getInstance().getTask().getSystemName()) && isEnabled(EventCode.CE_PERSON_MODIFICATION);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("group()")
    public Object doGroup(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        startGroup();
        return proceedingJoinPoint.proceed();
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("calculatingAmountExchange()")
    public Object doCalculatingAmountExchange(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return template(userEvent -> {
            userEvent.setGroup(getGroup());
            Object proceed = proceedingJoinPoint.proceed();
            StepTwo stepTwo = (StepTwo) proceedingJoinPoint.getThis();
            userEvent.setDescription(String.format("%s %s %s", stepTwo.getOperation().getCode().equals(OperationCode.SELL) ? "Sell" : "Buy",
                                                   stepTwo.getOperation().getSum(), stepTwo.getOperation().getCurrency().getIso()));
            String errorMessage = stepTwo.getErrorMessage();
            if (errorMessage != null) {
                userEvent.setStatus(EventStatus.ERROR);
                userEvent.setDescription(String.format("%s Error: %s", userEvent.getDescription(), errorMessage));
            }
            return proceed;
        }, EventCode.CE_CALCULATING_AMOUNT_EXCHANGE);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("cancelingOperation()")
    public Object doCancelingOperation(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return template(userEvent -> {
            userEvent.setGroup(getGroup());
            StepTwo stepTwo = (StepTwo) proceedingJoinPoint.getThis();
            String operationType = OperationCode.BUY.equals(stepTwo.getOperation().getCode()) ? "Buy" : "Sell";
            userEvent.setDescription(Optional.ofNullable(stepTwo.getRate()).map(rate -> String
                    .format("%s: %s %s. Bank rate: %s-%s (order #%s)", operationType, stepTwo.getOperation().getSum(),
                            stepTwo.getOperation().getCurrency().getIso(), stepTwo.getOperation().getRate(), stepTwo.getRate().getRuleName(),
                            stepTwo.getOperation().getOrder().getNumber())).orElse(String.format("%s: %s %s. Bank rate not set", operationType,
                                                                                                 stepTwo.getOperation().getSum(),
                                                                                                 stepTwo.getOperation().getCurrency().getIso())));
            return proceedingJoinPoint.proceed();
        }, EventCode.CE_CANCELING_OPERATION);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("operationStarted()")
    public Object doOperationStarted(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return template(userEvent -> {
            userEvent.setGroup(getGroup());
            StepFour stepFour = (StepFour) proceedingJoinPoint.getThis();
            Object proceed = proceedingJoinPoint.proceed();
            userEvent.setDescription(String.format("%s: %s %s. Bank rate:%s (order #%s). Operation ID: %s",
                                                   OperationCode.BUY.equals(stepFour.getOperation().getCode()) ? "Buy" : "Sell",
                                                   stepFour.getOperation().getSum(), stepFour.getOperation().getCurrency().getIso(),
                                                   stepFour.getOperation().getRate(), stepFour.getOperation().getOrder().getNumber(),
                                                   stepFour.getOperation().getId()));
            String errorMessage = stepFour.getErrorMessage();
            if (errorMessage != null) {
                userEvent.setStatus(EventStatus.ERROR);
                userEvent.setDescription(String.format("%s Error: %s", userEvent.getDescription(), errorMessage));
            }
            return proceed;
        }, EventCode.CE_OPERATION_STARTED);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("reportReprint()")
    public Object doReportReprint(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return template(userEvent -> {
            userEvent.setGroup(getGroup());
            StepFour stepFour = (StepFour) proceedingJoinPoint.getThis();
            userEvent.setDescription(String.format("Operation ID: %s", stepFour.getOperation().getId()));
            return proceedingJoinPoint.proceed();
        }, EventCode.CE_REPORT_REPRINT);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("operationComplete()")
    public Object doOperationComplete(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return template(userEvent -> {
            userEvent.setGroup(getGroup());
            StepFour stepFour = (StepFour) proceedingJoinPoint.getThis();
            userEvent.setDescription(String.format("%s: %s %s. Bank rate:%s (order #%s). Operation ID: %s",
                                                   OperationCode.BUY.equals(stepFour.getOperation().getCode()) ? "Buy" : "Sell",
                                                   stepFour.getOperation().getSum(), stepFour.getOperation().getCurrency().getIso(),
                                                   stepFour.getOperation().getRate(), stepFour.getOperation().getOrder().getNumber(),
                                                   stepFour.getOperation().getId()));
            return proceedingJoinPoint.proceed();
        }, EventCode.CE_OPERATION_COMPLETE);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("personSearch()")
    public Object doPersonSearch(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return template(userEvent -> {
            userEvent.setGroup(getGroup());
            ClientSearchView searchView = (ClientSearchView) proceedingJoinPoint.getThis();
            userEvent.setDescription(String.format("%s: %s %s", documentTypeDictionary.findOne(searchView.getDocument().getType()).getValue(),
                                                   searchView.getDocument().getSeries(), searchView.getDocument().getNumber()));
            return proceedingJoinPoint.proceed();
        }, EventCode.CE_PERSON_SEARCH);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("personAddition()")
    public Object doPersonAddition(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return template(userEvent -> {
            userEvent.setGroup(getGroup());
            ClientSearchView searchView = (ClientSearchView) proceedingJoinPoint.getThis();
            userEvent.setDescription(String.format("%s: %s %s", documentTypeDictionary.findOne(searchView.getDocument().getType()).getValue(),
                                                   searchView.getDocument().getSeries(), searchView.getDocument().getNumber()));
            return proceedingJoinPoint.proceed();
        }, EventCode.CE_ADDING_PERSON);
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("personCreatedOrModified()")
    public Object doPersonCreatedOrUpdated(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        EditClientView editClientView = (EditClientView) proceedingJoinPoint.getThis();
        Object proceed;
        if (editClientView.getPerson().getId() == null && isEnabled(EventCode.CE_CREATING_PERSON)) {
            proceed = template(userEvent -> {
                userEvent.setGroup(getGroup());
                userEvent.setDescription(
                        String.format("%s: %s %s.", documentTypeDictionary.findOne(editClientView.getMainDocument().getType()).getValue(),
                                      editClientView.getMainDocument().getSeries(), editClientView.getMainDocument().getNumber()));
                return proceedingJoinPoint.proceed();
            }, EventCode.CE_CREATING_PERSON);
        } else if (isEnabled(EventCode.CE_PERSON_MODIFIED)) {
            proceed = template(userEvent -> {
                userEvent.setGroup(getGroup());
                userEvent.setDescription(String.format("%s: %s %s. Client ID: %s",
                                                       documentTypeDictionary.findOne(editClientView.getMainDocument().getType()).getValue(),
                                                       editClientView.getMainDocument().getSeries(), editClientView.getMainDocument().getNumber(),
                                                       editClientView.getPerson().getId()));
                return proceedingJoinPoint.proceed();
            }, EventCode.CE_PERSON_MODIFIED);
        } else {
            proceed = proceedingJoinPoint.proceed();
        }
        return proceed;
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("personModification()")
    public Object doPersonModification(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return template(userEvent -> {
            userEvent.setGroup(getGroup());
            ClientSearchView searchView = (ClientSearchView) proceedingJoinPoint.getThis();
            userEvent.setDescription(
                    String.format("%s: %s %s. Client ID: %s.", documentTypeDictionary.findOne(searchView.getDocument().getType()).getValue(),
                                  searchView.getDocument().getSeries(), searchView.getDocument().getNumber(), searchView.getPerson().getId()));
            return proceedingJoinPoint.proceed();
        }, EventCode.CE_PERSON_MODIFICATION);
    }
}
