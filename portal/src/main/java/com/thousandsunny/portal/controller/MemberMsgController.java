package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.ModuleKey.MemberMsgType;
import com.thousandsunny.service.model.JobApplyRecord;
import com.thousandsunny.service.model.MemberMsg;
import com.thousandsunny.service.service.JobApplyRecordService;
import com.thousandsunny.service.service.MemberMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


/**
 * Created by admin on 2016/11/7.
 */
@RestController
@RequestMapping(value = "/api/portal/msg", produces = APPLICATION_JSON_UTF8_VALUE)
public class MemberMsgController {
    String[] msg_list_json = {
            "type",
            "content",
            "isReadBoolean:isRead",
    };

    String[] msg_ats_json = {
            "id",
            "moments.id:talkId",
            "date",
            "moments.author.token:token",
            "moments.author.headImage.path:headerImageUrl",
            "moments.author.realName:realName",
            "moments.author.username:nickName",
            "isReadBoolean:isRead",
    };

    String[] msg_job_json = {
            "id",
            "date",
            "job.name:jobName",
            "content",
            "isReadBoolean:isRead",
            "job.recType:jobType"
    };

    String[] msg_work_state_json = {
            "id",
            "date",
            "content",
            "isReadBoolean:isRead",
            "remindType",
            "jobApplyRecord.recState:workState",
            "jobApplyRecord.id:recommendId",
    };

    String[] msg_friend_json = {
            "id",
            "friendApply.applicant.token:token",
            "friendApply.applicant.headImage.path:headerImageUrl",
            "friendApply.applicant.realName:realName",
            "friendApply.applyState:state",
            "isReadBoolean:isRead",
    };

    String[] is_not_read_message_json = {
            "talkCount",
            "cooperateCount",
            "transferCount",
            "cardAndTicketCount",
            "platformActivityCount"
    };


    @Autowired
    private MemberMsgService memberMsgService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;

    /**
     * 消息列表
     */
    @RequestMapping(value = "/list", method = GET)
    public ResponseEntity list(String userToken) {
        List<MemberMsg> msgs = memberMsgService.getMemberMsgs(userToken);
        List<JSONObject> jsonObjects = simpleMap(msgs, e -> propsFilter(e, msg_list_json));
        return ok(listToJson(jsonObjects));
    }

    /**
     * 消息列表-说说AT提醒
     */
    @RequestMapping(value = "/ats", method = GET)
    public ResponseEntity ats(String userToken, PageVO pageVO) {
        Page<MemberMsg> msgs = memberMsgService.getMemberMsgsByType(userToken, TALK_AT_REMIND, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(msgs, e -> propsFilter(e, msg_ats_json));
        return ok(jsonObject);
    }

    /**
     * 消息列表-岗位招聘预付款项提醒
     */
    @RequestMapping(value = "/repays", method = GET)
    public ResponseEntity repays(String userToken, PageVO pageVO) {
        Page<MemberMsg> msgs = memberMsgService.getMemberMsgsByType(userToken, JOB_PREPAY_REMIND, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(msgs, e -> propsFilter(e, msg_job_json));
        return ok(jsonObject);
    }

    /**
     * 消息列表-岗位招聘余额扣款提醒
     */
    @RequestMapping(value = "/charge", method = GET)
    public ResponseEntity charge(String userToken, PageVO pageVO) {
        Page<MemberMsg> msgs = memberMsgService.getMemberMsgsByType(userToken, JOB_CHARGEBACK_REMIND, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(msgs, e -> propsFilter(e, msg_job_json));
        return ok(jsonObject);
    }

    /**
     * 消息列表-岗位招聘违约提示提醒
     */
    @RequestMapping(value = "/default", method = GET)
    public ResponseEntity defaultMsg(String userToken, PageVO pageVO) {
        Page<MemberMsg> msgs = memberMsgService.getMemberMsgsByType(userToken, JOB_DEFAULT_REMIND, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(msgs, e -> propsFilter(e, msg_job_json));
        return ok(jsonObject);
    }

    /**
     * 消息列表-好友上班提醒
     */
    @RequestMapping(value = "/work", method = GET)
    public ResponseEntity work(String userToken, PageVO pageVO) {
        Page<MemberMsg> msgs = memberMsgService.getMemberMsgsByType(userToken, FRIEND_WORK_REMIND, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(msgs, e -> {
            JSONObject jo = propsFilter(e, msg_job_json);
            if(isNotNull(e.getJobApplyRecord()))
                jo.put("recommendWorkId", e.getJobApplyRecord().getId());
            else jo.put("recommendWorkId", null);
            return jo;
        });
        return ok(jsonObject);
    }

    /**
     * 消息列表-工作状态确认提醒
     */
    @RequestMapping(value = "/state", method = GET)
    public ResponseEntity state(String userToken, PageVO pageVO) {
        Page<MemberMsg> msgs = memberMsgService.getMemberMsgsByType(userToken, WORK_STATE_CONFIRM_REMIND, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(msgs, e -> propsFilter(e, msg_work_state_json));
        return ok(jsonObject);
    }

    /**
     * 消息列表-慧友请求提醒
     */
    @RequestMapping(value = "/friend", method = GET)
    public ResponseEntity friend(String userToken, PageVO pageVO) {
        Page<MemberMsg> msgs = memberMsgService.getMemberMsgsByType(userToken, H_FRIEND_APPLY_REMIND, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(msgs, e -> propsFilter(e, msg_friend_json));
        return ok(jsonObject);
    }

    /**
     * 19.1发现模块未读消息数量
     */
    @RequestMapping(value = "/isNotReadMessageAcount", method = GET)
    public ResponseEntity applicationMessageAcount(String userToken) {
        Map<String, Long> map = memberMsgService.isNotReadMessageAcount(userToken);
        JSONObject jo = propsFilter(map, is_not_read_message_json);
        return ok(jo);
    }


    /**
     * 19.2未读消息一键阅读
     */
    @RequestMapping(value = "/oneKeyReading", method = POST)
    public ResponseEntity oneKeyReading(String userToken, MemberMsgType type) {
        memberMsgService.oneKeyReading(userToken, type);
        return OK;
    }

    /**
     *19.12 消息列表是否有未读消息
     */
    @RequestMapping(value = "/unReadMsg", method = GET)
    public ResponseEntity unReadMsg(String userToken) {
        List<MemberMsg> msgs = memberMsgService.getMemberMsgs(userToken);
        final boolean[] isHasUnRead = {false};
        msgs.forEach(x -> {
            if (!x.getIsReadBoolean()) {
                isHasUnRead[0] = true;
                return;
            }
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isHasUnRead", isHasUnRead[0]);
        return ok(jsonObject);
    }

    /**
     *19.11 我的消息-未读消息置为已读
     */
    @RequestMapping(value = "/readingMsg",method = POST)
    public ResponseEntity readingMsg(String userToken, MemberMsgType type){
        memberMsgService.readingMsg(userToken, type);
        return OK;
    }

}
