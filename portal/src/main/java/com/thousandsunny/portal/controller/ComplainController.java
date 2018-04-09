package com.thousandsunny.portal.controller;

import com.thousandsunny.service.service.ComplainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.core.ModuleKey.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static com.thousandsunny.service.ModuleKey.ComplainType;

/**
 * Created by admin on 2016/11/7.
 */
@RestController
@RequestMapping(value = "/api/portal/complain", produces = APPLICATION_JSON_UTF8_VALUE)
public class ComplainController {

    @Autowired
    private ComplainService complainService;

    @RequestMapping(value = "/complain", method = POST)
    public ResponseEntity save(String userToken, String complaintedUserToken, Long complaintedSchoolId, Long complaintedStoreId, ComplainType type, String reasons) {
        complainService.save(userToken, complaintedUserToken, complaintedSchoolId, complaintedStoreId, type, reasons);
        return OK;
    }
}
