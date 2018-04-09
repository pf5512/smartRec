package com.thousandsunny.portal.controller.dto;

import com.thousandsunny.service.model.ResumeTrainExp;

import java.util.Comparator;

/**
 * Created by admin on 2016/11/21.
 */
public class ComparatorTrain implements Comparator<ResumeTrainExp> {
    public int compare(ResumeTrainExp o1, ResumeTrainExp o2) {
        return o2.getStartDate().compareTo(o1.getStartDate());
    }

}
