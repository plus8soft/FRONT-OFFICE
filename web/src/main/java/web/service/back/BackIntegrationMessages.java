/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.back;

public final class BackIntegrationMessages {

    public static final String CORE_NOT_CONNECTED =
            "Core banking is not connected. Implement an adapter in the *BackService classes "
                    + "(web.service.back.*) — see INTEGRATION_AND_ARCHITECTURE.md.";

    private BackIntegrationMessages() {
    }
}
