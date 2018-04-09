/* *
 * Ping++ Server SDK
 * 说明：
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可根据自己网站需求按照技术文档编写, 并非一定要使用该代码。
 * 接入退款流程参考开发者中心：https://www.pingxx.com/docs/server/refund ，文档可筛选后端语言和接入渠道。
 * 该代码仅供学习和研究 Ping++ SDK 使用，仅供参考。
 */
package com.thousandsunny.thirdparty.pingpp;

import com.pingplusplus.exception.*;
import com.pingplusplus.model.Charge;
import com.pingplusplus.model.ChargeRefundCollection;
import com.pingplusplus.model.Refund;

import java.util.HashMap;
import java.util.Map;

/**
 * 退款相关示例
 * <p>
 * 该实例程序演示了如何从 Ping++ 服务器进行退款操作。
 * <p>
 * 开发者需要填写 apiKey 和 chargeId ,
 * <p>
 * apiKey 有 TestKey 和 LiveKey 两种。
 * <p>
 * TestKey 模式下不会产生真实的交易。
 */
public class RefundService {

    private Charge charge;

    RefundService(String chargeId) {
        try {
            this.charge = Charge.retrieve(chargeId);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | ChannelException | APIException e) {
            e.printStackTrace();
        }
    }

    public static void runDemos() {
        String chargeId = "ch_5CWrz5rnz9GS84arXHLiPOqL";
        RefundService refundExample = new RefundService(chargeId);
        System.out.println("------- 创建 refund -------");
        Refund refund = refundExample.refund(1);
        System.out.println("------- 查询 refund -------");
        refundExample.retrieve(refund.getId());
        System.out.println("------- 查询 refund 列表 -------");
        refundExample.all();
    }

    /**
     * 退款
     * <p>
     * 创建退款，需要先获得 charge ,然后调用 charge.getRefunds().create();
     * 参数具体说明参考：https://pingxx.com/document/api#api-r-new
     * <p>
     * 可以一次退款，也可以分批退款。
     *
     * @param amount
     * @return
     */
    public Refund refund(Integer amount) {
        if (charge == null) {
            return null;
        }
        Refund refund = null;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("description", "苹果被咬了一口。");
        params.put("amount", amount);// 退款的金额, 单位为对应币种的最小货币单位，例如：人民币为分（如退款金额为 1 元，此处请填 100）。必须小于等于可退款金额，默认为全额退款

        try {
            refund = charge.getRefunds().create(params);
            System.out.println(refund);
        } catch (AuthenticationException | APIException | ChannelException | APIConnectionException | InvalidRequestException e) {
            e.printStackTrace();
        }
        return refund;
    }

    /**
     * 查询退款
     * <p>
     * 根据 Id 查询退款记录。需要传递 charge。
     * 参考文档：https://pingxx.com/document/api#api-r-inquiry
     *
     * @param id
     */
    public void retrieve(String id) {
        if (charge == null) {
            return;
        }
        try {
            Refund refund = charge.getRefunds().retrieve(id);
            System.out.println(refund);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | APIException | ChannelException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分页查询退款
     * <p>
     * 批量查询退款。默认一次 10 条，用户可以通过 limit 自定义查询数目，但是最多不超过 100 条。
     * 参考文档：https://pingxx.com/document/api#api-r-list
     */
    public void all() {
        if (charge == null) {
            return;
        }
        Map<String, Object> refundParams = new HashMap<String, Object>();
        refundParams.put("limit", 3);
        try {
            ChargeRefundCollection refunds = charge.getRefunds().all(refundParams);
            System.out.println(refunds);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | APIException | ChannelException e) {
            e.printStackTrace();
        }
    }
}
