package com.trace.config;

import com.alibaba.fastjson2.JSON;
import com.trace.common.OperationLog;
import com.trace.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 操作日志AOP切面
 * 拦截带有@OperationLog注解的方法，自动记录操作日志
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private LogService logService;

    @Around("@annotation(com.trace.common.OperationLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        String operation = annotation.value();
        String methodName = point.getTarget().getClass().getName() + "." + method.getName();
        String params = "";
        try {
            Object[] args = point.getArgs();
            // 只序列化前两个参数，避免过长
            if (args != null && args.length > 0) {
                params = JSON.toJSONString(args.length > 2 ? new Object[]{args[0], args[1]} : args);
                if (params.length() > 500) {
                    params = params.substring(0, 500) + "...";
                }
            }
        } catch (Exception e) {
            params = "参数序列化失败";
        }

        try {
            Object result = point.proceed();
            // 记录成功日志
            logService.recordLog(operation, methodName, params, 1, null);
            return result;
        } catch (Exception e) {
            // 记录失败日志
            logService.recordLog(operation, methodName, params, 0, e.getMessage());
            throw e;
        }
    }
}
