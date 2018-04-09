package com.thousandsunny.portal.controller.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by admin on 2016/11/25.
 */
@Data
public class RewardHelp {
    private String relationshipType;
    private String name;
    private String hpAccount;
    private BigDecimal reward;

}
