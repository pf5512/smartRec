package com.thousandsunny.thirdparty.domain.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.SmsService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.thirdparty.domain.repository.ThirdPartySocialAccountRepository;
import com.thousandsunny.thirdparty.model.ThirdPartySocialAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.google.common.collect.ImmutableBiMap.of;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.core.ModuleKey.SmsType.BIND_MOBILE;
import static com.thousandsunny.core.ModuleKey.ThirdPartySocialAccountType.*;
import static com.thousandsunny.core.ModuleKey.ToggleAction;
import static com.thousandsunny.core.ModuleKey.ToggleAction.BIND;
import static com.thousandsunny.thirdparty.ModuleTips.*;
import static java.util.Objects.isNull;

@Service
public class ThirdPartySocialAccountService extends BaseService<ThirdPartySocialAccount> {

    @Autowired
    private ThirdPartySocialAccountRepository thirdPartySocialAccountRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BaseMemberService memberService;
    @Autowired
    private SmsService smsService;


    /**
     * 第三方登陆
     */
    public Member findByTpAccount(ThirdPartySocialAccount tPSocialAccount) {
        ThirdPartySocialAccount memberSocialAccountRef = getThirdPartySocialAccount(tPSocialAccount);
        ifNullThrow(memberSocialAccountRef, TIP_TP_NOT_BIND);
        return memberSocialAccountRef.getMember();
    }

    /**
     * 第三方账号绑定/解绑
     */
    public ResponseEntity bindOrUnbindThirdParty(ToggleAction toggleAction, String userToken, ThirdPartySocialAccount tPSocialAccount) {
        Member member = memberService.findByToken(userToken);
        if (BIND == toggleAction) {
            ThirdPartySocialAccount thirdPartySocialAccount = getThirdPartySocialAccount(tPSocialAccount);
            ifNotNullThrow(thirdPartySocialAccount, TIP_TP_HAS_BIND);
            tPSocialAccount.setMember(member);
            thirdPartySocialAccountRepository.save(tPSocialAccount);
            return OK;
        } else {
            ThirdPartySocialAccount thirdPartySocialAccount = thirdPartySocialAccountRepository.findByMemberAndAccountType(member, tPSocialAccount.getAccountType());
            ifNullThrow(thirdPartySocialAccount, TIP_HAS_UNBINDED);
            thirdPartySocialAccountRepository.delete(thirdPartySocialAccount);
            return OK;
        }
    }

    /**
     * 第三方登陆绑定手机
     */
    public Member thirdPartyBindMobile(ThirdPartySocialAccount thirdPartySocialAccount, String mobile, String code) {
        smsService.validateReceiverAndCode(mobile, code, BIND_MOBILE);
        Member member = memberRepository.findByMobile(mobile);
        if (isNull(member))//如果用户不存在则创建
            member = memberService.initialWithRandomInfo(mobile);
        thirdPartySocialAccount.setMember(member);
        ThirdPartySocialAccount tpSocialAccount = getThirdPartySocialAccount(thirdPartySocialAccount);
        ifNotNullThrow(tpSocialAccount, TIP_TP_HAS_BIND);
        thirdPartySocialAccountRepository.save(thirdPartySocialAccount);
        return member;
    }

    public ThirdPartySocialAccount getThirdPartySocialAccount(ThirdPartySocialAccount thirdPartySocialAccount) {
        Specification spec = mapToEqualSpec(of("accountType", thirdPartySocialAccount.getAccountType()
//                , "accountName", thirdPartySocialAccount.getAccountName()
                , "accountIdentity", thirdPartySocialAccount.getAccountIdentity()
                , "active", YES));
        return thirdPartySocialAccountRepository.findOne(spec);
    }


    /**
     * 放进去第三方的登陆信息
     */
    public JSONObject wrapThirdPartyInfo(Member member, JSONObject mappedMember) {
        ThirdPartySocialAccount qq = thirdPartySocialAccountRepository.findByMemberAndAccountType(member, QQ);
        ThirdPartySocialAccount wx = thirdPartySocialAccountRepository.findByMemberAndAccountType(member, WX);
        ThirdPartySocialAccount wb = thirdPartySocialAccountRepository.findByMemberAndAccountType(member, WB);
        ThirdPartySocialAccount wxPub = thirdPartySocialAccountRepository.findByMemberAndAccountType(member, WX_PUB);
        mappedMember.put("isBindQQ", !isNull(qq));
        mappedMember.put("qqName", isNull(qq) ? null : decodePathVariable(qq.getAccountName()));
        mappedMember.put("isBindWX", !isNull(wx));
        mappedMember.put("wxName", isNull(wx) ? null : decodePathVariable(wx.getAccountName()));
        mappedMember.put("isBindWB", !isNull(wb));
        mappedMember.put("wbName", isNull(wb) ? null : decodePathVariable(wb.getAccountName()));
        mappedMember.put("isBindWXPub", !isNull(wxPub));
        mappedMember.put("wxPubName", isNull(wxPub) ? null : decodePathVariable(wxPub.getAccountName()));
        return mappedMember;
    }

}
