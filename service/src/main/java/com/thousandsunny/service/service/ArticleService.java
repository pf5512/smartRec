package com.thousandsunny.service.service;

import com.thousandsunny.cms.domain.repository.ArticleRepository;
import com.thousandsunny.cms.domain.repository.ChannelRepository;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.ArticleRead;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.repository.DocumentFileRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.DocumentFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.repository.ArticleReadRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

/**
 * Created by ekoo on 2016/12/14.
 */
@Service
public class ArticleService extends BaseService<Article> {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private DocumentFileRepository documentFileRepository;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private ArticleReadRepository articleReadRepository;

    public Page<Article> articleList(BackPageVo pageVO, String text, Long classId) {

        Specification<Article> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotNullThen(classId, t -> predicates.add(rb.equal(rt.get("channel").get("id"), t)));
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("title"), "%" + t + "%"), rb.like(rt.get("channel").get("name"), "%" + t + "%"))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("publishTime"), false)).getRestriction();
        };
        return articleRepository.findAll(specification, pageVO.pageRequest());
    }

    public List<Article> findArticleList(Long classId) {
        return articleRepository.findByChannelIdAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(classId, NO, NO);
    }

    public void delArticle(Long[] ids) {
        for (long id : ids) {
            Article article = articleRepository.findOne(id);
            article.setIsDelete(YES);
        }
    }

    public void persistArticle(Long id, String classId, String title, Long sort, String coverImg, Date publishTime, String content) throws ParseException {
        Article article = null;
        if (id == null) {
            article = new Article();
        } else {
            article = articleRepository.findOne(id);
        }
        String str = classId;
        if (classId.contains(",")) {
            str = classId.substring(classId.lastIndexOf(",") + 1);
        }
        Long typeId = Long.parseLong(str);
        Channel channel = channelRepository.findOne(typeId);
        article.setChannel(channel);
        article.setTitle(title);
        article.setContent(content);
        article.setWeight(sort);
        DocumentFile documentFile = documentFileRepository.findTop1ByPath(coverImg);
        if (documentFile != null) {
            article.setCoverImage(documentFile);
        } else {
            if (!"".equals(coverImg)) {
                documentFile = new DocumentFile();
                documentFile.setPath(coverImg);
                DocumentFile d = documentFileRepository.save(documentFile);
                article.setCoverImage(d);
            }
        }
        article.setPublishTime(publishTime);
        articleRepository.save(article);
    }

    public Article articleDetails(Long id) {
        return articleRepository.findOne(id);
    }

    public Page<Article> activityList(Pageable pageable, Long classId) {
        Set<Long> channelIds = findChannelIds(20L);
        channelIds.add(20L);
        Specification<Article> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            if (isNotNull(classId)) predicates.add(rb.equal(rt.get("channel").get("id"), classId));
            else predicates.add(rt.get("channel").get("id").in(channelIds));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("publishTime"), false)).getRestriction();
        };
        return articleRepository.findAll(specification, pageable);
    }

    public Boolean judgeIsRead(Member member, Article e) {
        ArticleRead articleRead = articleReadRepository.findByMemberTokenAndArticleId(member.getToken(), e.getId());
        if (isNotNull(articleRead) && articleRead.getIsDelete() == NO && articleRead.getIsRead() == YES) return true;
        else return false;
    }

    public Set<Long> findMyReadedArticles(Member member, Long ChannelId) {
        Set<Long> readedArticleIds = new HashSet<>();
        Set<Long> platFormActivityChannelIds = findChannelIds(ChannelId);
        List<ArticleRead> list = articleReadRepository.
                findByMemberIdAndArticleChannelIdInAndIsDeleteAndIsRead(member.getId(), platFormActivityChannelIds, NO, YES);
        if (!list.isEmpty()) list.forEach(articleRead -> readedArticleIds.add(articleRead.getArticle().getId()));
        return readedArticleIds;
    }

    public Set<Long> findChannelIds(Long parentChannelId) {
        Set<Long> chanelIds = new HashSet<>();
        List<Channel> list = channelRepository.findByParentChannelIdAndIsDelete(parentChannelId, NO);
        if (!list.isEmpty()) list.forEach(channel -> chanelIds.add(channel.getId()));
        for (Channel channel : list) {
            Set<Long> childs = findChannelIds(channel.getId());
            if (!childs.isEmpty()) chanelIds.addAll(childs);
        }
        return chanelIds;
    }

    public List<Article> findNeedReadArticles(Set<Long> readedArticleIds, Set<Long> platformActivityChannelIds) {
        return articleRepository.findByChannelIdInAndIsDeleteAndIdNotIn(platformActivityChannelIds, NO, readedArticleIds);
    }
}
