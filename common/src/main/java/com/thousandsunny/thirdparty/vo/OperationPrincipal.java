package com.thousandsunny.thirdparty.vo;

import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import lombok.Data;

import java.util.List;

/**
 * 用于接收接口6.12
 * Created by mu.jie on 2016/9/23.
 */
@Data
public class OperationPrincipal {
    private String userToken;
    private OperatorType operatorType;
    private List<Principal> tokens;

}
