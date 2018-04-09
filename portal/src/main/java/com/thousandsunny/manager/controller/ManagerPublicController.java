package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.common.HTMLUtil;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey.ApplyState;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.DocumentFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.ModuleKey.JobConstantEnum;
import com.thousandsunny.service.model.JobConstant;
import com.thousandsunny.service.model.JobType;
import com.thousandsunny.service.model.LinePaymentBank;
import com.thousandsunny.service.model.TransferRecord;
import com.thousandsunny.service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.ApplyState.*;
import static com.thousandsunny.core.ModuleKey.CategoryType;
import static com.thousandsunny.service.ModuleTips.TIP_NO_CHANSHUERROR;
import static com.thousandsunny.service.ModuleTips.TIP_NO_MEMBER;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@RestController
@RequestMapping(value = "/api/manager/public", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerPublicController {

    private static final String[] TRANSFER_RECORD_JSON = {
            "shopName:shopname",
            "assignor.realName:older",
            "assignor.mobile:tel1",
            "assignor.hpAccount:hpAcount1",
            "receiverRealName:newer",
            "receiverPhoneNumber:tel2",
            "receiverHpAccount:hpAcount2",
            "excuse:transferContent"
    };

    private static String[] transfer_record_list_json = {
            "id",
            "shop.name:shopname",
            "assignor.realName:oldAdministrator",
            "assignor.hpAccount:oldHp",
            "receiverRealName:assignee",
            "receiverHpAccount:newHp"
    };

    private static String[] MEMBER_JSON = {
            "headImage.path:img",
            "vipId:acountID",
            "mobile:tel",
            "realName:name",
            "username:nickname",
            "gender.title:sex",
            "hpAccount:hpAcount"
    };

    private static String[] CHANNEL_JSON = {
            "parentChannel.id:parentId",
            "name:channelName",
            "weight:sort",
            "content:description"
    };

    private static String[] ARTICLE_JSON = {
            "id",
            "title",
            "channel.name:class",
            "weight:sort"

    };

    private static String[] ARTICLE_DETAILS_JSON = {
            "channel.id:classId",
            "title",
            "weight:sort",
            "content"
    };

    @Autowired
    private RegionService regionService;
    @Autowired
    private DBSelectService dbSelectService;
    @Autowired
    private JobConstantService jobConstantService;
    @Autowired
    private TransferRecordService transferRecordService;
    @Autowired
    private JobTypeService jobTypeService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private LinePaymentbankService linePaymentbankService;


    /**
     * 1.1 公共类别树
     */
    @RequestMapping(value = "/publicTypeTree", method = GET)
    public Result publicTypeTree(String userToken, CategoryType type) {
        return OK(jobTypeService.jobTypeTree(userToken));
    }


    /**
     * 1.2 省市区联动
     */
    @RequestMapping(value = "/region", method = GET)
    public ResponseEntity region(Long provinceId, Long cityId) {
        List<Region> list = regionService.region(provinceId, cityId);
        String[] REGION_JSON = {"id", "name"};
        List<JSONObject> jsonObjects = simpleMap(list, x -> propsFilter(x, REGION_JSON));
        return ok(listToJson(jsonObjects));
    }

    /**
     * 1.3 数据库式下拉框
     */
    @RequestMapping(value = "/dbSelect", method = GET)
    public Result dbSelect(String type) {
        return OK(dbSelectService.dbSelect(type));
    }


    /**
     * 2.1.1 新增/修改类别
     */
    @RequestMapping(value = "/persistJobType", method = POST)
    public Result persistJobType(Long id, String parentsChannel, String channelName, String userToken) {
        jobTypeService.persistJobType(id, parentsChannel, channelName);
        return OK();
    }

    /**
     * 2.1.2 删除类别
     */
    @RequestMapping(value = "/delJobType", method = POST)
    public Result delJobType(Long id) {
        jobTypeService.delJobType(id);
        return OK();
    }

    /**
     * 2.1.3 类别信息
     */
    @RequestMapping(value = "/findJobType", method = GET)
    public Result findJobType(Long id) {
        JobType jobType = jobTypeService.findJobType(id);
        String[] JOB_TYPE_JSON = {"parentJobType.id:parentId", "name:channelName"};
        JSONObject jo = propsFilter(jobType, JOB_TYPE_JSON);
        return OK(jo);
    }

    /**
     * 2.1.4 薪资待遇（工作经验）列表
     */
    @RequestMapping(value = "/findSalaryList", method = GET)
    public ResponseEntity findSalaryOrWorkExperienceList(JobConstantEnum type) {
        List<JobConstant> jobConstants = jobConstantService.findSalaryOrWorkExperienceList(type);
        String[] job_constant_json = {"id", "name:text"};
        List<JSONObject> jsonObjects = simpleMap(jobConstants, x -> propsFilter(x, job_constant_json));
        return ok(listToJson(jsonObjects));
    }

    /**
     * 2.1.5 薪资待遇(工作经验)删除
     */
    @RequestMapping(value = "/delSalaryOrWorkExperience", method = POST)
    public Result delSalaryOrWorkExperience(long[] id) {
        jobConstantService.delSalaryOrWorkExperience(id);
        return OK();
    }

    /**
     * 2.1.6 新增/修改薪资待遇（工作经验）
     */
    @RequestMapping(value = "/persistSalaryOrWorkExperience", method = POST)
    public Result persistSalaryOrWorkExperience(String range, JobConstantEnum type, Long id) {
        jobConstantService.persistSalaryOrWorkExperience(range, type, id);
        return OK();
    }

    /**
     * 2.2.1 店铺管理权转让列表
     */
    @RequestMapping(value = "/transferRecordList", method = GET)
    public Result findTransferRecordList(BackPageVo backPageVo, String text, ApplyState auditStatus, ApplyState tableType) {
        Page<TransferRecord> transferRecordList = transferRecordService.findTransferRecordList(backPageVo, text, auditStatus, tableType);
        Page<JSONObject> jsonObjects = transferRecordList.map(e -> {
            JSONObject jsonObject = propsFilter(e, transfer_record_list_json);
            jsonObject.put("date", ISO_DATETIME_FORMAT.format(e.getDate()));
            if (tableType == APPLY) {
                JSONObject json = new JSONObject();
                json.put("key", "waitAudit");
                json.put("text", "待审核");
                jsonObject.put("state", json);
            }
            if (tableType == APPROVAL) {
                JSONObject obj = new JSONObject();
                if (e.getState() == AGREE) {
                    obj.put("key", "pass");
                    obj.put("text", "已通过");
                }
                if (e.getState() == REJECT) {
                    obj.put("key", "nopass");
                    obj.put("text", "已拒绝");
                }
                jsonObject.put("state", obj);
            }
            return jsonObject;
        });
        return OK(jsonObjects);
    }

    /**
     * 2.2.2 删除店铺管理权记录
     */
    @RequestMapping(value = "/transferRecord", method = DELETE)
    public Result delTransferRecord(String id) {
        transferRecordService.delTransferRecord(id);
        return OK();
    }

    /**
     * 2.2.3 查看店铺管理权记录
     */
    @RequestMapping(value = "/transferRecord", method = GET)
    public Result findTransferRecord(Long id) {
        JSONObject json = new JSONObject();
        TransferRecord transferRecord = transferRecordService.findTransferRecord(id);
        ifNullThrow(transferRecord, TIP_NO_CHANSHUERROR);
        String phoneNumber = transferRecord.getReceiverPhoneNumber();
        Member receiver = memberService.findByPhone(phoneNumber);
        ifNullThrow(receiver, TIP_NO_MEMBER);
        JSONObject transferInformation = propsFilter(transferRecord, TRANSFER_RECORD_JSON);
        transferInformation.put("date", ISO_DATETIME_FORMAT.format(transferRecord.getDate()));
        JSONObject oldAdmin = getByMember(transferRecord.getAssignor());
        JSONObject newAdmin = getByMember(receiver);
        JSONObject auditStatus = new JSONObject();
        auditStatus.put("status", transferRecord.getState().name());
        String remark = transferRecord.getRemark() == null ? "" : transferRecord.getRemark();
        auditStatus.put("reason", remark);
        json.put("transferInformation", transferInformation);
        json.put("oldAdmin", oldAdmin);
        json.put("newAdmin", newAdmin);
        json.put("auditStatus", auditStatus);
        return OK(json);
    }

    private JSONObject getByMember(Member member) {
        JSONObject jsonObject = propsFilter(member, MEMBER_JSON);
        if (isNotNull(member.getUsername()))
            jsonObject.replace("nickname", HTMLUtil.decodePathVariable(member.getUsername()));
        MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(member.getToken());
        Date date = memberExtInfo.getRegisterTime();
        String regDate = date == null ? "" : ISO_DATETIME_FORMAT.format(date);
        Date b = member.getBirthday();
        String birthday = b == null ? "" : ISO_DATE_FORMAT.format(b);
        String referre = memberExtInfo.getRecommendUser() == null ? "" : memberExtInfo.getRecommendUser().getRealName();
        jsonObject.put("regDate", regDate);
        jsonObject.put("birthday", birthday);
        jsonObject.put("referre", referre);
        jsonObject.put("ischuangyeren", member.getEntrepreneurLevel().getTitle());
        jsonObject.put("ishehuoren", member.getPartnerLevel().getTitle());
        return jsonObject;
    }

    /**
     * 2.2.4 修改店铺管理权记录
     */
    @RequestMapping(value = "/transferRecord", method = POST)
    public Result updateTransferRecord(String reson, ApplyState auditStatus, Long id) {
        transferRecordService.updateTransferRecord(reson, auditStatus, id);
        return OK();
    }

    /**
     * 4.1.1 资讯类别树
     */
    @RequestMapping(value = "/channelTree", method = GET)
    public Result channelTree(String userToken) {
        return OK(channelService.channelTree(userToken));
    }

    /**
     * 4.1.2 删除资讯类别
     */
    @RequestMapping(value = "/delChannel", method = POST)
    public Result delChannel(Long id) {
        channelService.delChannel(id);
        return OK();
    }


    /**
     * 4.1.3 新增/修改
     */
    @RequestMapping(value = "/persistChannel", method = POST)
    public Result persistChannel(Long id, String parentsChannel, String channelName, Long sort, String description, Date publishTime) throws ParseException {
        channelService.persistChannel(id, parentsChannel, channelName, sort, description, publishTime);
        return OK();
    }


    /**
     * 4.1.4 类别信息
     */
    @RequestMapping(value = "/channelInfo", method = GET)
    public Result channelInfo(Long id) {
        Channel channel = channelService.channelInfo(id);
        JSONObject jo = propsFilter(channel, CHANNEL_JSON);
        Date publishTime = channel.getCreateTime();
        String publishTimeStr = publishTime == null ? "" : ISO_DATETIME_FORMAT.format(publishTime);
        jo.put("publishTime", publishTimeStr);
        return OK(jo);
    }

    /**
     * 4.2.1 资讯内容列表
     */
    @RequestMapping(value = "/articleList", method = GET)
    public Result articleList(BackPageVo pageVO, String text, Long classId) {
        Page<Article> articles = articleService.articleList(pageVO, HTMLUtil.decodePathVariable(text), classId);
        Page<JSONObject> jsonObjects = articles.map(e -> {
            JSONObject jsonObject = propsFilter(e, ARTICLE_JSON);
            ifNotNullThen(e.getPublishTime(), x -> jsonObject.put("publishTime", ISO_DATETIME_FORMAT.format(x)));
            ifNullThen(e.getPublishTime(), () -> jsonObject.put("publishTime", ""));
            return jsonObject;
        });
        return OK(jsonObjects);
    }

    /**
     * 4.2.2 删除资讯内容
     */
    @RequestMapping(value = "/delArticle", method = POST)
    public Result delArticle(Long[] id) {
        articleService.delArticle(id);
        return OK();
    }


    /**
     * 4.2.3 新增/修改资讯内容
     */
    @RequestMapping(value = "/persistArticle", method = POST)
    public Result persistArticle(Long id, String classId, String title, Long sort, String coverImg, Date publishTime, String content) throws ParseException {
        articleService.persistArticle(id, classId, title, sort, coverImg, publishTime, content);
        return OK();
    }

    /**
     * 4.2.4 资讯内容详情
     */
    @RequestMapping(value = "/articleDetails", method = GET)
    public Result articleDetails(Long id) {
        Article article = articleService.articleDetails(id);
        JSONObject jo = propsFilter(article, ARTICLE_DETAILS_JSON);
        DocumentFile documentFile = article.getCoverImage();
        if (documentFile != null) {
            jo.put("coverImg", documentFile.getPath());
        } else {
            jo.put("coverImg", null);
        }
        ifNotNullThen(article.getPublishTime(), x -> jo.put("publishTime", ISO_DATETIME_FORMAT.format(x)));
        ifNullThen(article.getPublishTime(), () -> jo.put("publishTime", null));
        return OK(jo);
    }

    /**
     * 1.8 平台银行列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/27
     */
    @RequestMapping(value = "/lineBankList", method = GET)
    public Result lineBankList() {
        String[] line_pay_bank_info = {"id", "bankName", "bankNo:cardNo",};
        List<LinePaymentBank> list = linePaymentbankService.findBankList();
        List<JSONObject> jsonObjects = simpleMap(list, bank -> propsFilter(bank, line_pay_bank_info));
        return OK(jsonObjects);
    }

}
