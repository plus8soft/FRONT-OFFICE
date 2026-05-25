/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.integration.fineract;

import java.io.Serializable;
import lombok.Getter;

@Getter
public class FineractClientStatus implements Serializable {

    public enum State {
        DISABLED,
        NOT_CONFIGURED,
        UNAVAILABLE,
        NOT_SYNCED,
        SYNCED,
        SYNC_ERROR
    }

    private final State state;

    private final String message;

    private final Long fineractClientId;

    private final String externalId;

    private FineractClientStatus(State state, String message, Long fineractClientId, String externalId) {
        this.state = state;
        this.message = message;
        this.fineractClientId = fineractClientId;
        this.externalId = externalId;
    }

    public static FineractClientStatus disabled() {
        return new FineractClientStatus(State.DISABLED, "Fineract integration is disabled", null, null);
    }

    public static FineractClientStatus notConfigured() {
        return new FineractClientStatus(State.NOT_CONFIGURED, "Fineract is not configured", null, null);
    }

    public static FineractClientStatus unavailable(String detail) {
        return new FineractClientStatus(State.UNAVAILABLE, "Fineract is not reachable" + (detail == null ? "" : ": " + detail),
                                        null, null);
    }

    public static FineractClientStatus notSynced(String externalId) {
        return new FineractClientStatus(State.NOT_SYNCED, "Client is not linked in Fineract yet", null, externalId);
    }

    public static FineractClientStatus synced(Long fineractClientId, String externalId) {
        return new FineractClientStatus(State.SYNCED, "Linked to Fineract", fineractClientId, externalId);
    }

    public static FineractClientStatus syncError(String detail) {
        return new FineractClientStatus(State.SYNC_ERROR, detail == null ? "Fineract sync error" : detail, null, null);
    }

    public boolean isShowOnCard() {
        return state != State.DISABLED && state != State.NOT_CONFIGURED;
    }

    public String getCssClass() {
        switch (state) {
            case SYNCED:
                return "success_title";
            case NOT_SYNCED:
            case UNAVAILABLE:
                return "warning_title";
            case SYNC_ERROR:
                return "error_title";
            default:
                return "";
        }
    }

    public String getSummary() {
        if (state == State.SYNCED) {
            return "Fineract: client #" + fineractClientId + " (" + externalId + ")";
        }
        return "Fineract: " + message;
    }
}
