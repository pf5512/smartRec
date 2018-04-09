package com.thousandsunny.common.exception;

import com.thousandsunny.common.entity.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.entity.Result.forbidden;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThen;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.status;

/**
 * Created by guitarist on 6/25/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@ResponseBody
@ControllerAdvice
public class ControllerAdviceHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public Result<String> badCredentialsException(BadCredentialsException e) {
        e.printStackTrace();
        return forbidden(e.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity baseException(BaseException exception) {
        return status(BAD_REQUEST).body(new _result(exception.getCode() + "", exception.getMessage()));
    }


    @ExceptionHandler(BindException.class)
    public ResponseEntity bindException(BindException exception) {
        return status(BAD_REQUEST).body(new _result("9977", exception.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity runtimeException(Exception exception) {
        exception.printStackTrace();
        StringBuilder builder = new StringBuilder("程序报错了,在抓紧修理!");
        builder.append("\n").append(exception.toString()).append("\n");
        newArrayList(exception.getStackTrace()).forEach(e -> {
            String str = e.toString();
            ifTrueThen(str.startsWith("com.thousandsunny"), () -> builder.append(str).append("\n"));
        });
        return status(INTERNAL_SERVER_ERROR).body(builder.toString());
    }
}

@Data
@AllArgsConstructor
class _result {
    private String code;
    private String message;
}
