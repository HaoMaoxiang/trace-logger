package tech.mervyn.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.mervyn.logger.LogMessage;
import tech.mervyn.logger.LogUtil;
import tech.mervyn.properties.TraceLogProperty;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * @author HaoMaoxiang@126.com
 * @since 2020/2/21
 */
@Aspect
@Component
public class WebLogAop {

    private final static Logger log = LoggerFactory.getLogger(WebLogAop.class);

    @Resource
    private TraceLogProperty logProperty;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)|| " +
            "@within(org.springframework.stereotype.Controller)")
    public void controllerAdvice() {
    }

    @Pointcut("@within(org.springframework.stereotype.Service) || " +
            "target(com.baomidou.mybatisplus.extension.service.impl.ServiceImpl)")
    public void serviceAdvice() {
    }

    @Around("controllerAdvice()")
    public Object controllerAroundAdvice(ProceedingJoinPoint point) throws Throwable {
        LocalDateTime start = LocalDateTime.now();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!logProperty.getControllerLog() || requestAttributes == null) {
            return point.proceed();
        }

        // 请求进入日志
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String host = request.getRemoteHost();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        Map<String, String[]> args = request.getParameterMap();
        log.info(LogUtil.logMsg("_com_request_in", "host", host, "method", method,
                "uri", uri, "args", args));

        Object proceed = point.proceed();

        // 请求离开日志
        log.info(LogUtil.logMsg("_com_request_out", "host", host, "method", method,
                "uri", uri, "response", proceed, "proc_time", LogUtil.procTime(start)));
        LogMessage.remove();
        return proceed;
    }

    @Around("serviceAdvice()")
    public Object serviceAroundAdvice(ProceedingJoinPoint point) throws Throwable {

        if (!logProperty.getServiceLog()) {
            return point.proceed();
        }

        LocalDateTime start = LocalDateTime.now();
        String className = point.getTarget().getClass().getName();
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        String methodName = methodSignature.getMethod().getName();
        Object[] args = point.getArgs();
        log.info(LogUtil.logMsg("_com_service_in", "class_name", className, "method_name", methodName,
                "args", args));
        Object proceed = point.proceed();
        log.info(LogUtil.logMsg("_com_service_out", "class_name", className, "method_name", methodName,
                "return", proceed, "proc_time", LogUtil.procTime(start)));
        return proceed;

    }

}
