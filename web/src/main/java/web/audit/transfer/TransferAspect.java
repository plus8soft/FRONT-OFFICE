/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.audit.transfer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Configurable;
import web.audit.EventAspect;

@Aspect
@Configurable
public class TransferAspect extends EventAspect {

    @Pointcut("execution(java.lang.String web.view.transfer.TransferView.send()) " +
              "|| execution(java.lang.String web.view.transfer.TransferView.get())")
    public static void group() {
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Around("group()")
    public Object doGroup(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        startGroup();
        return proceedingJoinPoint.proceed();
    }
}
