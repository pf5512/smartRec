package com.thousandsunny.cms.domain.controller;


import com.thousandsunny.cms.domain.service.BaseArticleService;
import com.thousandsunny.cms.domain.service.BaseChannelService;
import com.thousandsunny.cms.domain.service.CommentaryService;
import com.thousandsunny.cms.dto.IdNameMemo;
import com.thousandsunny.cms.dto.SysArticle;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.ArticleReq;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.entity.Result.OK;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by guitarist on 2016/4/7.
 */
@RestController
@RequestMapping(value = "/baseArticles", produces = APPLICATION_JSON_UTF8_VALUE)
public class BaseArticleController {

    @Autowired
    private BaseArticleService baseArticleService;
    @Autowired
    private CommentaryService commentaryService;
    @Autowired
    private BaseChannelService baseChannelService;

    @RequestMapping(method = GET)
    public Result findByName(String name) {
        return OK(baseArticleService.findByName(name));
    }

    /**
     * 关键字相似标题内容
     */
    @RequestMapping(value = "/{kw}", method = GET)
    public Result titleLike(@PathVariable String kw, Long[] cids, PageVO pageVO, String type) {
        List<Long> idContainer = newArrayList();
        recursiveChildChannelIds(idContainer, newArrayList(cids));//武警新闻,时时热点
        idContainer.addAll(newArrayList(cids));
        Page<Article> articles = baseArticleService.titleOrContentLikeAndChannelIdIn(decodePathVariable(kw), idContainer, pageVO.pageRequest(), type);
        return OK(articles);
    }

    private void recursiveChildChannelIds(List<Long> idContainers, List<Long> channelIds) {
        channelIds.forEach(parentId -> {
            List<Channel> childChannels = baseChannelService.childChannels(parentId);
            childChannels.forEach(child -> idContainers.add(child.getId()));
            recursiveChildChannelIds(idContainers, childChannels.stream().map(Channel::getId).collect(toList()));
        });
    }

    /**
     * 栏目下的新闻列表
     */
    @RequestMapping(value = "/content", method = GET)
    public Result getBy(Long id) {
        List<SysArticle> collect = baseArticleService.findByChannelId(id).stream().map(SysArticle::new).collect(toList());
        collect.forEach(sysArticle -> sysArticle.setChannelParent(null));
        return OK(collect);
    }

    /**
     * 保存资讯_不含文件
     */
    @RequestMapping(value = "/pure", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result save(Article article) {
        return OK(baseArticleService.save(article));
    }

    /**
     * 保存资讯
     */
    @RequestMapping(method = POST)
    public Result save(Article article, MultipartFile cif, MultipartFile rdf, ArticleReq req) {
        req.setCoverImageFile(cif);
        req.setRawDownFile(rdf);
        return OK(baseArticleService.save(article, req));
    }

    /**
     * 更新资讯
     */
    @RequestMapping(method = POST, params = "update")
    public Result update(Article article, MultipartFile cif, MultipartFile rdf, ArticleReq req) {
        req.setCoverImageFile(cif);
        req.setRawDownFile(rdf);
        return OK(baseArticleService.update(article, req));
    }

    @RequestMapping(method = DELETE, value = "/deleteImg")
    public void deleteImg(Long id) {
        baseArticleService.deleteImg(id);
    }

    /**
     * 新闻详情
     */
    @RequestMapping(value = "/{id}", method = GET)
    public Result findOne(@PathVariable Long id) {
        Article article = baseArticleService.findOne(id);
        Integer clickCount = article.getClickCount();
        article.setClickCount(clickCount == null ? 0 : clickCount + 1);
        baseArticleService.save(article);
        return OK(isNull(article) ? null : article.setCommentaries(commentaryService.findByArticleId(id)));
    }


    /**
     * 置顶
     */
    @RequestMapping(value = "/toTop", method = PUT)
    public Result toTopArticle(Long id) {
        return OK(baseArticleService.updateToTopArticle(id));
    }

    /**
     * 批量置顶
     */
    @RequestMapping(value = "/manyToTop", method = PUT)
    public void manyToTopArticle(String idArray) {
        baseArticleService.updateManyToTopArticle(idArray);
    }

    /**
     * 推荐
     */
    @RequestMapping(value = "/toRecommend", method = PUT)
    public Result toggleRecommend(Long id) {
        return OK(baseArticleService.updateRecommendArticle(id));
    }

    /**
     * 批量推荐
     */
    @RequestMapping(value = "/manyToRecommend", method = PUT)
    public void manyToRecommend(String idArray) {
        baseArticleService.updateManyRecommendArticle(idArray);
    }

    /**
     * 推荐
     */
    @RequestMapping(value = "/{id}", method = PUT, params = "toggleAudited")
    public Result toggleAudited(@PathVariable Long id) {
        return OK(baseArticleService.toggleAudited(id));
    }

    /**
     * 相关推荐
     */
    @RequestMapping(value = "/related/{id}", method = GET)
    public Result related(@PathVariable Long id) {
        return OK(baseArticleService.relatedRecommendation(id).stream().map(a -> new IdNameMemo(a.getId(), a.getTitle())).collect(toList()));
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/{id}", method = DELETE)
    public void deleteArticle(@PathVariable Long id) {
        baseArticleService.deleteArticleFake(id);
    }

    /**
     * 批量删除
     */
    @RequestMapping(method = DELETE, params = "batch")
    public void delManyArticle(String idArray) {
        baseArticleService.delManyArticle(idArray);
    }

    /**
     * 查询
     */
    @RequestMapping(value = "/search", method = GET)
    public Result<SysArticle> search(Long channelId, String keyWord) {
        return baseArticleService.search(channelId, decodePathVariable(keyWord));
    }

    /**
     * 回收站列表
     */
    @RequestMapping(value = "/trash", method = GET)
    public Result allTrash() {
        return baseArticleService.findAllTrash();
    }

    /**
     * 从回收站删除
     */
    @RequestMapping(value = "/delArticleFromTrash", method = DELETE)
    public void delArticleFromTrash(long id) {
        baseArticleService.delete(id);
    }

    /**
     * 批量从回收站删除
     */
    @RequestMapping(value = "/delManyArticleFromTrash", method = DELETE)
    public void delManyArticleFromTrash(String idArray) {
        baseArticleService.delManyArticleFromTrash(idArray);
    }

    /**
     * 还原
     */
    @RequestMapping(value = "/reback", method = PUT)
    public void rebackAricle(Long id) {
        baseArticleService.rebackAricle(id);
    }

    /**
     * 还原多个
     */
    @RequestMapping(value = "rebackMany", method = PUT)
    public void rebackManyAricle(String idString) {
        baseArticleService.rebackManyAricle(idString);
    }

    /**
     * 查询回收站
     */
    @RequestMapping(value = "/findTrash", method = GET)
    public Result searchTrash(Long channelId, String keyWord) {
        return baseArticleService.srarchTrash(decodePathVariable(keyWord));
    }

    /**
     * 复制和copy
     */
    @RequestMapping(value = "/moveOrCopy", method = PUT)
    public void moveOrCopyArticle(String articleId, Long channelId, String flag) {
        baseArticleService.updateMoveOrCopyArticle(articleId, channelId, flag);
    }

    /**
     * 上移
     */
    @RequestMapping(value = "/up", method = PUT)
    public void upMove(Long id, String upId) {
        baseArticleService.upMove(id, upId);
    }

    /**
     * 下移
     */
    @RequestMapping(value = "/down", method = PUT)
    public void downMove(Long id, String downId) {
        baseArticleService.downMove(id, downId);
    }


}