package com.thousandsunny.portal.controller.dto;

import lombok.Data;

import java.util.Comparator;

/**
 * Created by admin on 2016/11/21.
 */
@Data
public class ComparatorMember implements Comparator<LetterFriend> {
    public int compare(LetterFriend lhs, LetterFriend rhs) {
        LetterFriend user0 = lhs;
        LetterFriend user1 = rhs;
        if (user0.getLetter().equals("#"))  return 100;
        if (user1.getLetter().equals("#"))  return -100;
        else return user0.getLetter().compareTo(user1.getLetter());

    }

}
