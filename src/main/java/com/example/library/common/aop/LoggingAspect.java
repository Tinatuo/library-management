package com.example.library.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;


@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 20)
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("com.example.library.common.aop.Pointcuts.serviceLayer()")
    public Object traceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        String method = joinPoint.getSignature().toShortString();
        log.debug("-> {} args=[{}]", method, describeArguments(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            log.debug("<- {}", method);
            return result;
        } catch (Throwable ex) {
            log.debug("!! {} threw {}: {}", method, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private String describeArguments(Object[] args) {
        return Arrays.stream(args)
                .map(LoggingAspect::describeArgument)
                .collect(Collectors.joining(", "));
    }

    private static String describeArgument(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof Number || arg instanceof Boolean || arg instanceof Enum<?>) {
            return String.valueOf(arg);
        }
        return arg.getClass().getSimpleName();
    }
}
