package com.thousandsunny.service.service;

import com.thousandsunny.service.ModuleKey.RedPacketState;
import com.thousandsunny.service.model.CardCoupon;
import com.thousandsunny.service.model.CardCouponReceive;
import com.thousandsunny.service.model.RedPacket;
import com.thousandsunny.service.model.RedPacketReceive;
import com.thousandsunny.service.repository.CardCouponReceiveRepository;
import com.thousandsunny.service.repository.CardCouponRepository;
import com.thousandsunny.service.repository.RedPacketReceiveRepository;
import com.thousandsunny.service.repository.RedPacketRepository;
import com.thousandsunny.thirdparty.ModuleKey;
import com.thousandsunny.thirdparty.domain.repository.AccountFreezingRecordRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountRepository;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFreezingRecord;
import org.slf4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.DateUtil.subtractDay;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.CardCouponReceiveState.RECEIVED;
import static com.thousandsunny.service.ModuleKey.CardCouponReceiveState.RECEIVED_EXPIRE;
import static com.thousandsunny.service.ModuleKey.CardCouponState.EXPIRE;
import static com.thousandsunny.service.ModuleKey.CardCouponState.NORMAL;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateFormatUtils.formatUTC;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * 如果这些代码有用，那它们是guitarist在17/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class ScheduleService {

    /**
     * "0 0/5 *  * * ?" 每5分钟执行一次
     * "0 0 8 ? * * " 每天8点触发,判断是否要给将要到期任务发送消息提醒
     * "0/55 * * * * ?" 每55秒执行一次
     * "0 0 12 * * ?" 每天中午12点触发
     * "0 15 10 ? * *" 每天上午10:15触发
     * "0 15 10 * * ?" 每天上午10:15触发
     * "0 15 10 * * ? *" 每天上午10:15触发
     * "0 15 10 * * ? 2005" 2005年的每天上午10:15触发
     * "0 * 14 * * ?" 在每天下午2点到下午2:59期间的每1分钟触发
     * "0 0/5 14 * * ?" 在每天下午2点到下午2:55期间的每5分钟触发
     * "0 0/5 14,18 * * ?" 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发
     * "0 0-5 14 * * ?" 在每天下午2点到下午2:05期间的每1分钟触发
     * "0 10,44 14 ? 3 WED" 每年三月的星期三的下午2:10和2:44触发
     * "0 15 10 ? * MON-FRI" 周一至周五的上午10:15触发
     * "0 15 10 15 * ?" 每月15日上午10:15触发
     * "0 15 10 L * ?" 每月最后一日的上午10:15触发
     * "0 15 10 ? * 6L" 每月的最后一个星期五上午10:15触发
     * "0 15 10 ? * 6L 2002-2005" 2002年至2005年的每月的最后一个星期五上午10:15触发
     * "0 15 10 ? * 6#3" 每月的第三个星期五上午10:15触发
     */


    private Logger logger = getLogger(getClass());
    @Autowired
    private RenewalsRecordService renewalsRecordService;
    @Autowired
    private BenefitService benefitService;
    @Autowired
    private AutomaticRenewalsService automaticRenewalsService;
    @Autowired
    private CardCouponRepository cardCouponRepository;
    @Autowired
    private CardCouponReceiveRepository cardCouponReceiveRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RedPacketReceiveRepository redPacketReceiveRepository;
    @Autowired
    private AccountFreezingRecordRepository accountFreezingRecordRepository;


    /**
     * 账户余额>=扣款金额
     * 1.账户扣钱
     * 2.系统账户价钱
     * 3.添加一条自动续费记录
     * <p>
     * 账户余额<扣款金额
     */
    // FIXME: 2017/1/10 正式时需要修改成一天执行一次。
//    @Scheduled(cron = "0 0/1 *  * * ?")
    @Scheduled(cron = "0 18 5  * * ?")
    private void chargeMonthlyFee() {
        logger.info("==================满一个月:扣费逻辑:开始==================");
        renewalsRecordService.chargeMonthlyFee();
        logger.info("==================满一个月:扣费逻辑:结束==================");
    }


    /**
     * 平台,合伙人,(车马费:上班的人)
     * 会员续的费,进行分钱
     * 推荐人        推荐关系    注册关系
     * 朋友（一级)   30%         10%
     * <p>
     * 熟人（二级)   10%         5%
     * <p>
     * 人脉（三级)   5%          5%
     */
//    @Scheduled(cron = "0 0/1 *  * * ?")
    @Scheduled(cron = "0 18 3 * * ?")
    private void spoilsDaily() {
        logger.info("==================满一个月:分赃逻辑:开始==================");
        renewalsRecordService.spoilsDaily();
        logger.info("==================满一个月:分赃逻辑:结束==================");
    }


    /**
     * 上班好处逻辑
     */
//    @Scheduled(cron = "0 0/1 *  * * ?")
    @Scheduled(cron = "0 20 2  * * ?")
    private void dealBenefit() {
        logger.info("==================上班好处逻辑:开始==================");
        benefitService.dealBenefit();
        logger.info("==================上班好处逻辑:结束==================");
    }

    /**
     * 重置好处申请，重置是否提醒，每天凌晨启动
     *
     * @Author mu.jie
     * @Date 2016/12/14
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void resetBenefitApply() {
        logger.info("==================上班好处申请重置逻辑:开始==================");
        benefitService.resetBenefitApply();
        logger.info("==================上班好处逻辑:结束==================");
    }

    /**
     * 岗位招聘预付款提醒
     *
     * @Author xiao xue wei
     * @Date 2017/1/3
     */
//    @Scheduled(cron = "0 0/1 * * * ?")
    @Scheduled(cron = "0 0 8  * * ?")
    private void sendJobPayMessage() {
        logger.info("==================岗位招聘预付款提醒逻辑:开始==================");
        automaticRenewalsService.sendRepayMessage();
        logger.info("==================岗位招聘预付款提醒逻辑:结束==================");
    }

    /**
     * 岗位招聘违约提示提醒
     *
     * @Author xiao xue wei
     * @Date 2017/1/9
     */
//    @Scheduled(cron = "0 0/1 * * * ?")
    @Scheduled(cron = "0 0 8  * * ?")
    private void sendDefaultMessage() {
        logger.info("==================岗位招聘违约提示提醒逻辑:开始==================");
        renewalsRecordService.sendDefaultMessage();
        logger.info("==================岗位招聘违约提示提醒逻辑:结束==================");
    }

    /**
     * 岗位招聘余额付款余额不足的七天内每天提示一次
     *
     * @Author xiao xue wei
     * @Date 2017/1/9
     */
//    @Scheduled(cron = "0 0/1 * * * ?")
    @Scheduled(cron = "0 0 9  * * ?")
    private void sendChargeMessage() {
        logger.info("==================岗位招聘违约提示提醒逻辑:开始==================");
        renewalsRecordService.sendChargeMsg();
        logger.info("==================岗位招聘违约提示提醒逻辑:结束==================");
    }

    /**
     * 判断卡劵是否过期，过期则将卡劵过期
     * 判断红包是否过期，过期则将红包过期
     *
     * @Author mu.jie
     * @Date 2017/2/15
     */
//    @Scheduled(cron = "0 0/1 * * * ?")
    @Scheduled(cron = "0 0 9  * * ?")
    private void updateCardCouponAndRedPacketState() {
        Boolean flag = false;
        //卡卷过期查询
        Date now = new Date();
        List<CardCoupon> cardCouponList = cardCouponRepository.findByStateAndIsDeleteAndIsStop(NORMAL, NO, NO);
        for (CardCoupon cardCoupon : cardCouponList) {
            Long time = cardCoupon.getValidDate().getTime();
            if (now.getTime() >= time) {
                flag = true;
                cardCoupon.setState(EXPIRE);
                List<CardCouponReceive> cardCouponReceiveList = cardCouponReceiveRepository.findByCardCouponIdAndStateAndIsDelete(cardCoupon.getId(), RECEIVED, NO);
                for (CardCouponReceive cardCouponReceive : cardCouponReceiveList) {
                    cardCouponReceive.setIsOverdue(YES);
                    cardCouponReceive.setState(RECEIVED_EXPIRE);
                }
                cardCouponReceiveRepository.save(cardCouponReceiveList);
            }
        }
        ifTrueThen(flag, () -> cardCouponRepository.save(cardCouponList));
        flag = false;
        //红包过期查询
        List<RedPacketReceive> redPacketReceiveList = redPacketReceiveRepository.findByStateAndValidDateBetween(RedPacketState.NORMAL, subtractDay(now, 1), now);
        for (RedPacketReceive redPacketReceive : redPacketReceiveList) {
            Long time = redPacketReceive.getRedPacket().getValidDate().getTime();
            if (now.getTime() >= time) {
                redPacketReceive.setState(RedPacketState.EXPIRE);
                flag = true;
            }
        }
        ifTrueThen(flag, () -> redPacketReceiveRepository.save(redPacketReceiveList));

    }

    /**
     * 解冻账户金额
     * 定时器分赃：个人收益15天内不能提现
     * 判断是个人收益是否超过15天，超过15天则解冻
     *
     * @Author mu.jie
     * @Date 2017/2/27
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void unfreezeAccountAmount() {
        Date now = new Date();
        List<AccountFreezingRecord> list = accountFreezingRecordRepository.findByIsUnfreezeAndUnfreezeDateBetween(NO, now, addDays(now, 1));
        for (AccountFreezingRecord record : list) {
            if (record.getUnfreezeDate().getTime() > now.getTime()) {
                record.setIsUnfreeze(YES);
                Account account = record.getAccount();
                account.setFreezingAmount(account.getFreezingAmount().subtract(record.getAmount()));
                accountRepository.save(account);
            }
        }
        accountFreezingRecordRepository.save(list);
    }

}
