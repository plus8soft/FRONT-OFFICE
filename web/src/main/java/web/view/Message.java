/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public interface Message {

    default List<FacesMessage> errorMessages() {
        return FacesContext.getCurrentInstance().getMessageList().stream()
                           .filter(facesMessage -> FacesMessage.SEVERITY_ERROR.equals(facesMessage.getSeverity())).collect(Collectors.toList());
    }

    default List<FacesMessage> fatalMessages() {
        return FacesContext.getCurrentInstance().getMessageList().stream()
                           .filter(facesMessage -> FacesMessage.SEVERITY_FATAL.equals(facesMessage.getSeverity())).collect(Collectors.toList());
    }

    default FacesMessage message(FacesMessage.Severity severity, String summary, String detail) {
        return new FacesMessage(severity, summary, detail);
    }

    default FacesMessage infoMessage(String summary, String detail) {
        return message(FacesMessage.SEVERITY_INFO, summary, detail);
    }

    default FacesMessage infoMessage(String summary) {
        return infoMessage(summary, null);
    }

    default FacesMessage infoMessageFormated(String pattern, Object... arguments) {
        return infoMessage(MessageFormat.format(pattern, arguments), null);
    }

    default FacesMessage warnMessage(String summary) {
        return message(FacesMessage.SEVERITY_WARN, summary, null);
    }

    default void addWarnMessage(String summary) {
        addMessage(null, warnMessage(summary));
    }

    default FacesMessage errorMessage(String summary, String detail) {
        return message(FacesMessage.SEVERITY_ERROR, summary, detail);
    }

    default FacesMessage errorMessage(String summary) {
        return errorMessage(summary, null);
    }

    default FacesMessage errorMessageFormated(String pattern, Object... arguments) {
        return errorMessage(MessageFormat.format(pattern, arguments), null);
    }

    default FacesMessage fatalMessage(String summary, String detail) {
        return message(FacesMessage.SEVERITY_FATAL, summary, detail);
    }

    default FacesMessage fatalMessage(String summary) {
        return fatalMessage(summary, null);
    }

    default FacesMessage fatalMessageFormated(String pattern, Object... arguments) {
        return fatalMessage(MessageFormat.format(pattern, arguments), null);
    }

    default void addMessage(String clientId, FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(clientId, message);
    }

    default void addInfoMessage(String summary) {
        addMessage(null, infoMessage(summary));
    }

    default void addInfoMessageFormated(String pattern, Object... arguments) {
        addInfoMessage(MessageFormat.format(pattern, arguments));
    }

    default void addErrorMessage(String summary) {
        addMessage(null, errorMessage(summary));
    }

    default void addErrorMessageFormated(String pattern, Object... arguments) {
        addErrorMessage(MessageFormat.format(pattern, arguments));
    }

    default void addFatalMessage(String summary) {
        addMessage(null, fatalMessage(summary));
    }

    default void addFatalMessageFormated(String pattern, Object... arguments) {
        addFatalMessage(MessageFormat.format(pattern, arguments));
    }
}
