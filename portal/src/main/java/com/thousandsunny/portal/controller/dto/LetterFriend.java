package com.thousandsunny.portal.controller.dto;

import com.thousandsunny.core.model.Member;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2016/10/21.
 */
@Data
public class LetterFriend {
    private String letter;
    private List<Member> member;

}
