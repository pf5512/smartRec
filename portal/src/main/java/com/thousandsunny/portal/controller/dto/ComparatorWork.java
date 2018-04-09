package com.thousandsunny.portal.controller.dto;

import com.thousandsunny.service.model.ResumeWorkExp;

import java.util.Comparator;

/**
 * Created by admin on 2016/11/21.
 */
public class ComparatorWork implements Comparator<ResumeWorkExp> {
    public int compare(ResumeWorkExp o1, ResumeWorkExp o2) {
        return o2.getStartDate().compareTo(o1.getStartDate());
    }

}
