package org.eclipse.jgit.telemetry.aspect;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class TelemetryAspect {

    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("appsmith");
    private static final long DEFAULT_THRESHOLD_NS = 1_000_000L;
    private static final long thresholdNanoseconds = loadThreshold();

    @Pointcut("execution(* org.eclipse.jgit..*(..))")
    public void allMethods() {}

    @Pointcut("execution(* org.eclipse.jgit.telemetry.aspect..*(..))")
    public void excludedPackages() {}

    @Pointcut("allMethods() && !excludedPackages()")
    public void monitoredMethods() {}

    @Around("monitoredMethods()")
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object result;
        Throwable error = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long duration = System.nanoTime() - start;
            if (duration > thresholdNanoseconds) {
                recordTrace(joinPoint, duration, error);
            }
        }

        return result;
    }

    private void recordTrace(ProceedingJoinPoint joinPoint, long duration, Throwable error) {
        String spanName = getSpanName(joinPoint);
        Span span = tracer.spanBuilder(spanName).startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("execution.time.ns", duration);
            if (error != null) {
                span.recordException(error);
            }
            log(span, spanName, duration);
        } finally {
            span.end();
        }
    }

    private void log(Span span, String spanName, long duration) {
        System.out.printf("🚀 %s > %s > %s took: %s%n",
                span.getSpanContext().getTraceId(),
                span.getSpanContext().getSpanId(),
                spanName,
                formatDuration(duration));
    }

    private String formatDuration(long nanoseconds) {
        double ms = nanoseconds / 1_000_000.0;
        return String.format("%.2f ms", ms);
    }

    private String getSpanName(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringTypeName() + "." +
               joinPoint.getSignature().getName();
    }

    private static long loadThreshold() {
        String raw = System.getenv("TELEMETRY_THRESHOLD_NANOSECONDS");

        if (raw == null) {
            return DEFAULT_THRESHOLD_NS;
        }

        raw = raw.trim();
        if (raw.isEmpty()) {
            return DEFAULT_THRESHOLD_NS;
        }

        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return DEFAULT_THRESHOLD_NS;
        }
    }
}
