package com.thousandsunny.common.exception;

import com.thousandsunny.common.entity.ModuleTip;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThrow;

/**
 * 如果这些代码有用，那它们是guitarist在04/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
//@Aspect
//@Component
public class AopValidatingAdvice {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Around("execution(public * com.thousandsunny..*Controller.*(..))&&args(..,bindingResult)")
    public Object doBefore(ProceedingJoinPoint joinPoint, BindingResult bindingResult) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        logger.info("URL : " + request.getRequestURL().toString());
        logger.info("HTTP_METHOD : " + request.getMethod());
        logger.info("IP : " + request.getRemoteAddr());
        logger.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        logger.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));

        ifTrueThrow(bindingResult.hasErrors(), new ModuleTip() {
            @Override
            public String getCode() {
                return "8997";
            }

            @Override
            public String getMessage() {
                return bindingResult.getAllErrors().get(0).getDefaultMessage();
            }
        });
        return joinPoint.proceed();
    }

}
