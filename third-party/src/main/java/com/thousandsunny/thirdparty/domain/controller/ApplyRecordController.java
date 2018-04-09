package com.thousandsunny.thirdparty.domain.controller;

import com.pingplusplus.model.Charge;
import com.pingplusplus.model.Transfer;
import com.thousandsunny.thirdparty.domain.service.TpAccountApplyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by guitarist on 7/14/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@RestController
public class ApplyRecordController {

    @Autowired
    private TpAccountApplyRecordService accountApplyService;

    @RequestMapping(value = "/pingpp/charge", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity pay(Charge charge, String userToken) {
        return ok(accountApplyService.createCharge(charge, userToken));
    }

    @RequestMapping(value = "/pingpp/withdraw", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity withdraw(Transfer transfer, String userToken) {
        return ok(accountApplyService.createWithdraw(transfer, userToken));
    }

    @RequestMapping(value = "/pingpp/callBack", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public void rechargeCallBack(HttpServletRequest request, HttpServletResponse response) {
        accountApplyService.callBack(request, response);
    }
}
