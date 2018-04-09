import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.config.BaseTransactionTest;
import com.thousandsunny.core.domain.service.ALiSmsService;
import com.thousandsunny.core.model.Human;
import com.thousandsunny.service.service.MemberRecRelService;
import com.thousandsunny.service.service.MemberRegRelService;
import com.thousandsunny.service.service.MemberService;
import com.thousandsunny.service.service.RenewalsRecordService;
import com.thousandsunny.thirdparty.easemob.comm.body.ChatGroupBody;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.ResponseWrapper;
import com.thousandsunny.thirdparty.easemob.service.EasemobService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.thousandsunny.common.JsonUtil.prettyPrinter;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static com.thousandsunny.core.ModuleKey.SmsType.BIND_MOBILE;

/**
 * 如果这些代码有用，那它们是guitarist在09/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class TestService extends BaseTransactionTest {

    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private EasemobService easemobService;
    @Autowired
    private ALiSmsService aLiSmsSender;

    @Test
    public void testRegRelLevel() {
        System.out.println(memberRegRelService.regRelLevel(token(22l), token(44l)));
        System.out.println(memberRegRelService.regRelLevel(token(22l), token(48l)));
        System.out.println(memberRegRelService.regRelLevel(token(22l), token(163l)));
    }

    @Test
    public void testRecRelLevel() {
        System.out.println(memberRecRelService.recRelLevel(token(22l), token(23l)));
        System.out.println(memberRecRelService.recRelLevel(token(22l), token(2l)));
        System.out.println(memberRecRelService.recRelLevel(token(22l), token(7l)));
    }

    private String token(Long id) {
        return memberService.findOne(id).getToken();
    }

    @Test
    public void testALiSmsSender() {
        aLiSmsSender.sendContent("15988498727", "SMS_33210096", BIND_MOBILE);
    }

    @Test
    public void testRegisterEasemobUser() {
        simpleMap(memberService.findAll(), Human::getToken).forEach(easemobService::registerEasemobUser);
    }

    @Test
    public void testEasePassword() {
        System.out.println(easemobService.password("668278a2c595439593d01f9ad2e6acd4"));
    }

    @Test
    public void testCreateChatGroup() {
        ChatGroupBody chatGroupBody = new ChatGroupBody("测试群组", "ASC", true, 200l, true, null, new String[]{"79caf1600cc24b269501bab4edaa238a"});
        ResponseWrapper s = easemobService.createChatGroup(chatGroupBody);
        JSONObject jsonObject = parseObject(s.getResponseBody().toString());
        System.out.println(parseObject(jsonObject.getString("data")).getString("groupid"));
    }

    @Test
    public void testRecCascade() {
        prettyPrinter(memberRecRelService.recCascade(236l));
    }

    @Test
    public void testRegCascade() {
        prettyPrinter(memberRegRelService.regCascade(236l));
    }

    @Autowired
    private RenewalsRecordService renewalsRecordService;

    @Test
    public void testSpoilsDaily() {
        renewalsRecordService.spoilsDaily();
    }

}
