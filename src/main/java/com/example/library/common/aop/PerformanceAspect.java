package com.example.library.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 30)
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    private final long slowCallThresholdMs;

    public PerformanceAspect(@Value("${app.aop.slow-call-threshold-ms}") long slowCallThresholdMs) {
        this.slowCallThresholdMs = slowCallThresholdMs;
    }

    @Around("com.example.library.common.aop.Pointcuts.serviceLayer()")
    public Object warnIfSlow(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            if (elapsedMs > slowCallThresholdMs) {
                log.warn("Slow call: {} took {} ms (threshold {} ms)",
                        joinPoint.getSignature().toShortString(), elapsedMs, slowCallThresholdMs);
            }
        }
    }
}
