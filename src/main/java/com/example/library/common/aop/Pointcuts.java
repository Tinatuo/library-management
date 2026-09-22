package com.example.library.common.aop;

import org.aspectj.lang.annotation.Pointcut;


public final class Pointcuts {

    private Pointcuts() {
    }

    @Pointcut("execution(public * com.example.library..service..*.*(..))")
    public void serviceLayer() {
    }
}
