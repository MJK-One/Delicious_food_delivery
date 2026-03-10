package com.dfdt.delivery.common.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j(topic = "서비스")
public class LogAspect {
    @Before("@within(org.springframework.stereotype.Service)")
    public void logBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String url = "";
        String httpMethod = "";

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            url = request.getRequestURI();
            httpMethod = request.getMethod();
        }
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().toShortString();
        log.info("[실행][{}] {} -> {}/{}",httpMethod,url,className,methodName);
    }


    @AfterReturning(pointcut = "@within(org.springframework.stereotype.Service)")
    public void logAfterReturn(JoinPoint joinPoint) {
        log.info("[종료] 서비스 실행 완료");
    }
}