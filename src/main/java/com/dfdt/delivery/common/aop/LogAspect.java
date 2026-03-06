package com.dfdt.delivery.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j(topic = "서비스")
public class LogAspect {
    @Before("@within(org.springframework.stereotype.Service)")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("[실행] 서비스 실행 중: {}", methodName);
    }


    @AfterReturning(pointcut = "@within(org.springframework.stereotype.Service)")
    public void logAfterReturn(JoinPoint joinPoint) {
        log.info("[종료] 서비스 실행 완료");
    }
}