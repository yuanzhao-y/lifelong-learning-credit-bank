package com.zhousheng.llcb.service;

import com.zhousheng.llcb.entity.SysOperationLog;
import com.zhousheng.llcb.mapper.SysOperationLogMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private final SysOperationLogMapper operationLogMapper;

    public OperationLogAspect(SysOperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Around("within(com.zhousheng.llcb.controller..*)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long started = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            save(joinPoint, "success", null, started);
            return result;
        } catch (Throwable ex) {
            save(joinPoint, "fail", ex.getMessage(), started);
            throw ex;
        }
    }

    private void save(ProceedingJoinPoint joinPoint, String result, String errorMessage, long started) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SysOperationLog log = new SysOperationLog();
        log.setOperatorId(SecurityUtils.currentUserIdOrNull());
        log.setModule(joinPoint.getTarget().getClass().getSimpleName());
        log.setOperationType(signature.getMethod().getName());
        log.setOperationContent("耗时 " + (System.currentTimeMillis() - started) + "ms");
        if (request != null) {
            log.setRequestMethod(request.getMethod());
            log.setRequestUri(request.getRequestURI());
            log.setIpAddress(clientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }
        log.setResult(result);
        log.setErrorMessage(errorMessage);
        log.setOperatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
