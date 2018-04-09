package com.thousandsunny.common.exception;

import com.thousandsunny.common.entity.ModuleTip;
import lombok.Data;

import static com.thousandsunny.core.ModuleTips.TIP_COMMON_TIP;

/**
 * Created by guitarist on 7/1/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
public class BaseException extends RuntimeException {
    private ModuleTip tip = TIP_COMMON_TIP;

    public BaseException(String message) {
        super(message);
    }

    public BaseException(ModuleTip tip) {
        super(tip.getMessage());
        setTip(tip);
    }

    public String getCode() {
        return tip.getCode();
    }
}
