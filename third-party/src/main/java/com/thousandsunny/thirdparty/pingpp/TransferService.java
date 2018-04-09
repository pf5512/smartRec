/* *
 * Ping++ Server SDK
 * 说明：
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可根据自己网站需求按照技术文档编写, 并非一定要使用该代码。
 * 接入企业付款流程参考开发者中心：https://www.pingxx.com/docs/server/transfer ，文档可筛选后端语言和接入渠道。
 * 该代码仅供学习和研究 Ping++ SDK 使用，仅供参考。
*/
package com.thousandsunny.thirdparty.pingpp;

import com.pingplusplus.exception.*;
import com.pingplusplus.model.Transfer;
import com.pingplusplus.model.TransferCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.thousandsunny.common.lambda.LambdaUtil.ifFalseThrow;
import static com.thousandsunny.thirdparty.ModuleTips.TIPS_ERROR;

/**
 * 该实例演示如何使用 Ping++ 进行企业转账。
 * <p>
 * 开发者需要填写 apiKey ，openid 和 appId ,
 * <p>
 * apiKey 有 TestKey 和 LiveKey 两种。
 * <p>
 * TestKey 模式下不会产生真实的交易。
 * <p>
 * openid 是发送红包的对象在公共平台下的 openid ,获得 openid 的方法可以参考微信文档：http://mp.weixin.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.html
 */
@Service
public class TransferService {

    @Autowired
    private PingConfig pingConfig;

    /**
     * 接收者的 openid
     */
    public static String openid = "USER_OPENID";// 用户在商户微信公众号下的唯一标识，获取方式可参考 WxPubOAuthExample.java

    /**
     * 创建企业转账
     * <p>
     * 创建企业转账需要传递一个 map 给 Transfer.create();
     * map 填写的具体介绍可以参考：https://pingxx.com/document/api#api-t-new
     *
     * @return
     */
    public Transfer createTransfer(Transfer transfer) {
        Map<String, Object> transferMap = new HashMap<String, Object>();
        transferMap.put("channel", transfer.getChannel());// 目前支持 wx(新渠道)、 wx_pub
        transferMap.put("order_no", transfer.getOrderNo());// 企业转账使用的商户内部订单号。wx(新渠道)、wx_pub 规定为 1 ~ 50 位不能重复的数字字母组合
        transferMap.put("amount", transfer.getAmount());// 订单总金额, 人民币单位：分（如订单总金额为 1 元，此处请填 100,企业付款最小发送金额为 1 元）
        transferMap.put("type", "b2c");// 付款类型，当前仅支持 b2c 企业付款
        transferMap.put("currency", "cny");
        transferMap.put("recipient", openid);// 接收者 id， 为用户在 wx(新渠道)、wx_pub 下的 open_id
        transferMap.put("description", "慧聘提现");
        Map<String, String> app = new HashMap<String, String>();
        app.put("id", pingConfig.getAppId());
        transferMap.put("app", app);

        try {
            transfer = Transfer.create(transferMap);
            System.out.println(transfer);
        } catch (AuthenticationException | InvalidRequestException | APIException | APIConnectionException | ChannelException e) {
            e.printStackTrace();
            ifFalseThrow(false,TIPS_ERROR);
        }
        return transfer;
    }

    /**
     * 根据 ID 查询
     * <p>
     * 根据 ID 查询企业转账记录。
     * 参考文档：https://pingxx.com/document/api#api-t-inquiry
     *
     * @param id
     */
    public void retrieve(String id) {
        Map<String, Object> param = new HashMap<String, Object>();
        try {
            Transfer transfer = Transfer.retrieve(id, param);
            System.out.println(transfer);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        } catch (ChannelException e) {
            e.printStackTrace();
        }

    }

    /**
     * 批量查询
     * <p>
     * 批量查询企业转账记录，默认一次查询 10 条，用户可以使用 limit 自定义查询数目，但是最多不超过 100 条。
     */
    public void all() {
        Map<String, Object> parm = new HashMap<String, Object>();
        parm.put("limit", 3);

        try {
            TransferCollection transferCollection = Transfer.all(parm);
            System.out.println(transferCollection);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        } catch (ChannelException e) {
            e.printStackTrace();
        }
    }
}
