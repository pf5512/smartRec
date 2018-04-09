package com.thousandsunny.cms.dto;

import com.thousandsunny.core.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;

/**
 * Created by mu.jie on 2016/8/11 0011.
 */
@Data
@NoArgsConstructor
public class SysUser {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private String createTime;
    private boolean valid;

    public SysUser(User user) {
        id = user.getId();
        username = user.getUsername();
        realName = user.getRealName();
//        SimpleDateFormat sdf=new SimpleDateFormat(ISO_DATETIME_FORMAT.getPattern());
        ifNotNullThen(user.getCreateTime(), x -> this.setCreateTime(ISO_DATETIME_FORMAT.format(x)));
        if (user.getValid() == YES) {
            valid = true;
        } else {
            valid = false;
        }
    }
}
