package com.thousandsunny.portal.controller;

import com.thousandsunny.cms.domain.repository.ArticleRepository;
import com.thousandsunny.cms.model.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by mu.jie on 2016/11/30.
 */
@RestController
@RequestMapping(value = "/view", produces = TEXT_HTML_VALUE)
public class IndexController {

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * 新闻详情
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/news/{id}", method = GET)
    public ModelAndView newsDetail(@PathVariable Long id) {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(id);
        view.addObject("content", article.getContent());
        view.setViewName("/news");
        return view;
    }

    /**
     * 平台活动详情
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/activity/{id}", method = GET)
    public ModelAndView activityDetail(@PathVariable Long id) {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(id);
        view.addObject("content", article.getContent());
        view.setViewName("/activity");
        return view;
    }

    /**
     * 视频分享页
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/video/{id}", method = GET)
    public ModelAndView videoDetail(@PathVariable Long id) {
        ModelAndView view = new ModelAndView();
        view.setViewName("/video");
        return view;
    }

    /**
     * 注册协议（隐私条款）
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/registerProtocol", method = GET)
    public ModelAndView registerProtocolDetail() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(26L);
        view.addObject("content", article.getContent());
        view.setViewName("/registerProtocol");
        return view;
    }

    /**
     * 合伙人规则
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/partnerRule", method = GET)
    public ModelAndView partnerDetail() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(39L);
        view.addObject("content", article.getContent());
        view.setViewName("/partnerRule");
        return view;
    }

    /**
     * 创业者规则
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/entrepreneurRule", method = GET)
    public ModelAndView entrepreneurDetail() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(38L);
        view.addObject("content", article.getContent());
        view.setViewName("/entrepreneurRule");
        return view;
    }

    /**
     * 招聘规则
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/jobRule", method = GET)
    public ModelAndView jobRule() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(40L);
        view.addObject("content", article.getContent());
        view.setViewName("/jobRule");
        return view;
    }

    /**
     * 提现规则
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/withdrawRule", method = GET)
    public ModelAndView withdrawRule() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(34L);
        view.addObject("content", article.getContent());
        view.setViewName("/withdrawRule");
        return view;
    }

    /**
     * 上班好处-100元车旅费权益说明
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/workWelfareTaxiFeeInfo", method = GET)
    public ModelAndView workWelfareTaxiFeeInfo() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(29L);
        view.addObject("content", article.getContent());
        view.setViewName("/workWelfareTaxiFeeInfo");
        return view;
    }

    /**
     * 上班好处-10万工作意外险
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/workWelfareInsureInfo", method = GET)
    public ModelAndView workWelfareInsureInfo() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(30L);
        view.addObject("content", article.getContent());
        view.setViewName("/workWelfareInsureInfo");
        return view;
    }

    /**
     * 上班好处-3次免费培训/年
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/workWelfareFreeTrainInfo", method = GET)
    public ModelAndView workWelfareFreeTrainInfo() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(31L);
        view.addObject("content", article.getContent());
        view.setViewName("/workWelfareFreeTrainInfo");
        return view;
    }

    /**
     * 上班好处-工资保障
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/workWelfareWagesEnsureInfo", method = GET)
    public ModelAndView workWelfareWagesEnsureInfo() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(32L);
        view.addObject("content", article.getContent());
        view.setViewName("/workWelfareWagesEnsureInfo");
        return view;
    }

    /**
     * 上班好处-快速贷款
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/workWelfareFastLoanInfo", method = GET)
    public ModelAndView workWelfareFastLoanInfo() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(33L);
        view.addObject("content", article.getContent());
        view.setViewName("/workWelfareFastLoanInfo");
        return view;
    }

    /**
     * 职业规划说明
     *
     * @Author mu.jie
     * @Date 2016/11/30
     */
    @RequestMapping(value = "/careerPlan", method = GET)
    public ModelAndView careerPlan() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/careerPlan");
        return view;
    }

    /**
     * 岗位悬赏规则，一次性悬赏
     *
     * @Author mu.jie
     * @Date 2016/12/9
     */
    @RequestMapping(value = "/jobRewardRuleOnce", method = GET)
    public ModelAndView jobRewardRule() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(27L);
        view.addObject("content", article.getContent());
        view.setViewName("/jobRewardRuleOnce");
        return view;
    }

    /**
     * 岗位悬赏规则，按月悬赏
     *
     * @Author mu.jie
     * @Date 2016/12/9
     */
    @RequestMapping(value = "/jobRewardRuleMonthly", method = GET)
    public ModelAndView jobRewardRuleMonthly() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(28L);
        view.addObject("content", article.getContent());
        view.setViewName("/jobRewardRuleMonthly");
        return view;
    }

    /**
     * 邀请规则
     *
     * @Author mu.jie
     * @Date 2016/12/12
     */
    @RequestMapping(value = "/inviteRule", method = GET)
    public ModelAndView inviteRule() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(37L);
        view.addObject("content", article.getContent());
        view.setViewName("/inviteRule");
        return view;
    }

    /**
     * 违约金规则
     *
     * @Author mu.jie
     * @Date 2017/3/16
     */
    @RequestMapping(value = "/breachRule", method = GET)
    public ModelAndView breachRule() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(75L);
        view.addObject("content", article.getContent());
        view.setViewName("/breachRule");
        return view;
    }

    /**
     * 店铺卡券管理规则
     *
     * @Author xiao xue wei
     * @Date 2017/3/23
     */
    @RequestMapping(value = "/cardsManagementRule", method = GET)
    public ModelAndView cardsManagementRule() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(36L);
        view.addObject("content", article.getContent());
        view.setViewName("/cardsManagementRule");
        return view;
    }

    /**
     * 红包使用规则
     *
     * @Author xiao xue wei
     * @Date 2017/3/23
     */
    @RequestMapping(value = "/redPacketUseRule", method = GET)
    public ModelAndView redPacketUseRule() {
        ModelAndView view = new ModelAndView();
        Article article = articleRepository.findOne(35L);
        view.addObject("content", article.getContent());
        view.setViewName("/redPacketUseRule");
        return view;
    }

}
