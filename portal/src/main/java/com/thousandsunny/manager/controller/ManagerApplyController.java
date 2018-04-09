package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.service.service.EntrepreneursService;
import com.thousandsunny.service.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * 如果这些代码有用，那它们是guitarist在16/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/api/manager/apply", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerApplyController {
    @Autowired
    private EntrepreneursService entrepreneursService;
    @Autowired
    private PartnerService partnerService;


    /**
     * 创业者审核
     */
    @RequestMapping(value = "/entrepreneursApply", method = POST)
    public ResponseEntity reviewEntrepreneursApply(String userToken, Long id, OperatorType operatorType) {
        entrepreneursService.reviewEntrepreneursApply(userToken, id, operatorType);
        return OK;
    }

    /**
     * 合伙人审核
     */
    @RequestMapping(value = "/partnerApply", method = POST)
    public ResponseEntity reviewPartnerApply(String userToken, Long id, OperatorType operatorType) {
        partnerService.reviewPartnerApply(userToken, id, operatorType);
        return OK;
}
}
