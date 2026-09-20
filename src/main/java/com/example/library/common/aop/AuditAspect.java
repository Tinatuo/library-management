package com.example.library.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class AuditAspect {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private static final String ANONYMOUS = "anonymous";
    private static final Pattern LINE_BREAKS = Pattern.compile("[\\r\\n]+");

    private final ExpressionParser parser = new SpelExpressionParser();
    private final TemplateParserContext templateContext = new TemplateParserContext();
    private final Map<String, Expression> parsedTemplates = new ConcurrentHashMap<>();

    @AfterReturning("@annotation(audited)")
    public void recordSuccess(JoinPoint joinPoint, Audited audited) {
        AUDIT_LOG.info("action={} actor={} outcome=SUCCESS details=[{}]",
                audited.action(), currentActor(), renderDetails(joinPoint, audited));
    }

    @AfterThrowing(pointcut = "@annotation(audited)", throwing = "ex")
    public void recordFailure(JoinPoint joinPoint, Audited audited, Throwable ex) {
        AUDIT_LOG.warn("action={} actor={} outcome=FAILURE reason={} details=[{}]",
                audited.action(), currentActor(), ex.getClass().getSimpleName(),
                renderDetails(joinPoint, audited));
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return ANONYMOUS;
        }
        return sanitize(authentication.getName());
    }

    private String renderDetails(JoinPoint joinPoint, Audited audited) {
        String template = audited.details();
        if (template.isEmpty()) {
            return "";
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
            Object[] args = joinPoint.getArgs();
            for (int i = 0; parameterNames != null && i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
            Expression expression = parsedTemplates.computeIfAbsent(
                    template, t -> parser.parseExpression(t, templateContext));
            return sanitize(expression.getValue(context, String.class));
        } catch (Exception e) {
            log.warn("Could not evaluate audit details '{}' for action {}: {}",
                    template, audited.action(), e.getMessage());
            return "unavailable";
        }
    }


    private static String sanitize(String value) {
        return value == null ? "" : LINE_BREAKS.matcher(value).replaceAll("_");
    }
}
