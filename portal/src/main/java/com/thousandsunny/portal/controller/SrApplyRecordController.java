package com.thousandsunny.portal.controller;

import com.thousandsunny.service.service.SrAccountApplyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by guitarist on 7/14/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@RestController
@RequestMapping(value = "/sa", produces = APPLICATION_JSON_UTF8_VALUE)
public class SrApplyRecordController {

    @Autowired
    private SrAccountApplyRecordService srAccountApplyRecordService;

    @RequestMapping(value = "/pingpp/callBack", method = POST)
    public void rechargeCallBack(HttpServletRequest request, HttpServletResponse response) {
        srAccountApplyRecordService.completeCallBack(request, response);
    }
}
