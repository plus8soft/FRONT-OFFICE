/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package org.springframework.faces.webflow;

import javax.faces.context.FacesContext;
import org.springframework.webflow.execution.RequestContext;

public final class JsfRuntimeInformation {

    private JsfRuntimeInformation() {
    }

    public static boolean isAtLeastJsf22() {
        return true;
    }

    public static boolean isAtLeastJsf21() {
        return true;
    }

    public static boolean isAtLeastJsf20() {
        return true;
    }

    public static boolean isAtLeastJsf12() {
        return true;
    }

    public static boolean isLessThanJsf20() {
        return false;
    }

    public static boolean isMojarraPresent() {
        return true;
    }

    public static boolean isMyFacesPresent() {
        return false;
    }

    public static boolean isMyFacesInUse() {
        return false;
    }

    public static boolean isSpringPortletPresent() {
        return false;
    }

    public static boolean isPortletRequest(FacesContext context) {
        return false;
    }

    public static boolean isPortletRequest(RequestContext context) {
        return false;
    }

    public static boolean isPortletContext(Object nativeContext) {
        return false;
    }
}
