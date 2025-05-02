package org.eclipse.jgit.telemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Aspect to create spans for JGit method executions only if parent exists.
 */
@Aspect
public class JGitTelemetryAspect {

    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("org.eclipse.jgit");

    @Around("execution(* org.eclipse.jgit..*(..))")
    public Object createSpanForMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Span parent = Span.current();
        // System.out.println("[JGitTelemetryAspect] Parent Span ID: " + parent.getSpanContext().getSpanId());
        if (!parent.getSpanContext().isValid()) {
            return joinPoint.proceed();
        }

        Span span = tracer.spanBuilder(joinPoint.getSignature().toShortString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            return joinPoint.proceed();
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }
}
